import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnChanges,
  SimpleChanges,
  ViewEncapsulation,
} from '@angular/core';
import { ScrapedReadability, ScrapeResponse } from '../../graphql/types';
import { Maybe } from 'graphql/jsutils/Maybe';
import {
  ReaderLinkTarget,
  ReaderTextTransform,
} from '../../products/reader/reader.page';
import { isUndefined } from 'lodash-es';
import { isDefined } from '../../modals/feed-builder-modal/scrape-builder';
import { GqlFeedlessPlugins } from '../../../generated/graphql';

@Component({
  selector: 'app-reader',
  templateUrl: './reader.component.html',
  styleUrls: ['./reader.component.scss'],
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ReaderComponent implements OnChanges {
  @Input()
  scrapeResponse: ScrapeResponse;

  @Input()
  linkTarget: ReaderLinkTarget;

  @Input()
  verboseLink: boolean;

  @Input()
  textTransform: ReaderTextTransform;

  content: string;
  private useBionic: boolean;
  private openLinkInReader: boolean;
  private showLinksHostname: boolean;

  constructor(private readonly changeRef: ChangeDetectorRef) {}

  async ngOnChanges(changes: SimpleChanges): Promise<void> {
    if (changes.scrapeResponse && changes.scrapeResponse.currentValue) {
      this.scrapeResponse = changes.scrapeResponse.currentValue;
    }

    if (changes.linkTarget && changes.linkTarget.currentValue) {
      const currentLinkTarget: ReaderLinkTarget =
        changes.linkTarget.currentValue;
      this.openLinkInReader = currentLinkTarget === 'reader';
    }

    if (changes.textTransform && changes.textTransform.currentValue) {
      const currentTextTransform: ReaderTextTransform =
        changes.textTransform.currentValue;
      this.useBionic = currentTextTransform === 'bionic';
    }

    if (changes.verboseLink && !isUndefined(changes.verboseLink.currentValue)) {
      this.showLinksHostname = changes.verboseLink.currentValue;
    }

    this.content = this.getContent();
    this.changeRef.detectChanges();
  }

  hasReadability(): boolean {
    return isDefined(this.getReadability());
  }

  private getReadability(): Maybe<ScrapedReadability> {
    return JSON.parse(
      this.scrapeResponse.elements[0].selector.fields.find(
        (field) => field.name === GqlFeedlessPlugins.OrgFeedlessFulltext,
      ).value.one.data,
    ) as ScrapedReadability;
  }

  private getContent(): string {
    if (this.hasReadability()) {
      const document = new DOMParser().parseFromString(
        this.getReadability().content,
        'text/html',
      );
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
              ahref.insertAdjacentText(
                'afterend',
                ` (${new URL(url).hostname})`,
              );
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
          .flatMap((p) =>
            Array.from(p.childNodes).filter((it) => it.nodeType === 3),
          )
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
                  ]),
              ),
            );
          });
      }

      return document.body.innerHTML;
    }
  }
}
