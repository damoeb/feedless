import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  input,
  OnChanges,
  SimpleChanges,
  ViewEncapsulation,
} from '@angular/core';
import { ReaderLinkTarget, ReaderTextTransform } from '../../products/reader/reader-product.page';
import { isUndefined } from 'lodash-es';
import { ServerConfigService } from '../../services/server-config.service';
import { isDefined } from '../../types';
import { NgClass } from '@angular/common';
import { IonCol, IonRow } from '@ionic/angular/standalone';

@Component({
  selector: 'app-reader',
  templateUrl: './reader.component.html',
  styleUrls: ['./reader.component.scss'],
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgClass, IonRow, IonCol],
  standalone: true,
})
export class ReaderComponent implements OnChanges {
  private readonly serverConfig = inject(ServerConfigService);
  private readonly changeRef = inject(ChangeDetectorRef);

  readonly linkTarget = input<ReaderLinkTarget>('blank');

  readonly verboseLink = input<boolean>(true);

  readonly textTransform = input<ReaderTextTransform>('normal');

  readonly html = input.required<string>();

  readonly showImages = input<boolean>(false);

  content: string;
  private useBionic: boolean;
  private openLinkInReader: boolean;
  private showLinksHostname: boolean;

  async ngOnChanges(changes: SimpleChanges): Promise<void> {
    if (changes.linkTarget && changes.linkTarget.currentValue) {
      const currentLinkTarget: ReaderLinkTarget = changes.linkTarget.currentValue;
      this.openLinkInReader = currentLinkTarget === 'reader';
    }

    if (changes.textTransform && changes.textTransform.currentValue) {
      const currentTextTransform: ReaderTextTransform = changes.textTransform.currentValue;
      this.useBionic = currentTextTransform === 'bionic';
    }

    if (changes.verboseLink && !isUndefined(changes.verboseLink.currentValue)) {
      this.showLinksHostname = changes.verboseLink.currentValue;
    }

    this.content = this.getContent();
    this.changeRef.detectChanges();
  }

  hasReadability(): boolean {
    return isDefined(this.html);
  }

  private getContent(): string {
    if (this.hasReadability()) {
      const document = new DOMParser().parseFromString(this.html(), 'text/html');
      Array.from(document.body.querySelectorAll('img[src]'))
        .filter((img) => img.getAttribute('src').startsWith('http'))
        .forEach((img) => {
          img.setAttribute(
            'src',
            this.serverConfig.apiUrl +
              '/attachment/proxy?url=' +
              encodeURIComponent(img.getAttribute('src'))
          );
        });
      Array.from(document.body.querySelectorAll('a[href]')).forEach((ahref) => {
        ahref.setAttribute('referrerpolicy', 'no-referrer');
        const url = ahref.getAttribute('href');
        if (!url.startsWith('#')) {
          if (this.openLinkInReader) {
            ahref.setAttribute('href', '/?url=' + encodeURIComponent(url));
            ahref.removeAttribute('target');
          } else {
            ahref.setAttribute('target', '_blank');
          }
          if (this.showLinksHostname) {
            try {
              ahref.insertAdjacentText('afterend', ` (${new URL(url).hostname})`);
            } catch (e) {
              // ignore
            }
          }
        }
      });
      if (this.useBionic) {
        const el = (name: string, ...children: (Node | string)[]) => {
          const s = document.createElement(name);
          s.append(...children);
          return s;
        };

        Array.from(document.body.querySelectorAll('p,span,li,blockquote'))
          .flatMap((p) => Array.from(p.childNodes).filter((it) => it.nodeType === 3))
          .forEach((it) => {
            it.replaceWith(
              el(
                'span',
                ...it.textContent
                  .split(' ')
                  .flatMap((text) => [
                    el('strong', text.substring(0, Math.ceil(text.length / 2))),
                    text.substring(Math.ceil(text.length / 2), text.length),
                    ' ',
                  ])
              )
            );
          });
      }

      return document.body.innerHTML;
    }
  }
}
