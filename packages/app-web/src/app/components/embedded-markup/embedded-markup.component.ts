import {
  AfterViewInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  effect,
  ElementRef,
  EventEmitter,
  inject,
  Input,
  input,
  OnChanges,
  OnDestroy,
  OnInit,
  SimpleChanges,
  viewChild,
} from '@angular/core';
import { isDefined } from '../../types';
import { Embeddable } from '../embedded-image/embedded-image.component';
import { SourceBuilder } from '../interactive-website/source-builder';
import { debounce, distinct, interval, Subscription } from 'rxjs';
import { NgClass, NgStyle } from '@angular/common';

export function transformXpathToCssPath(xpath: string): string {
  const cssPath = xpath
    .split('/')
    .filter((node) => node && node.length > 0)
    .map((node) => node.replace('[', ':nth-of-type(').replace(']', ')'))
    .join('> ')
    .replace(/^\./, ':scope');
  // if (cssPath.trim() === '.') {
  //   return ':scope';
  // }
  return cssPath;
}

function makeid(length: number) {
  let result = '';
  const characters =
    'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  const charactersLength = characters.length;
  for (let i = 0; i < length; i++) {
    result += characters.charAt(Math.floor(Math.random() * charactersLength));
  }
  return result;
}

interface IframeMessage {
  id: string;
  type: 'height' | 'xpath' | 'show-boxes';
  data: string | number | boolean;
}

@Component({
  selector: 'app-embedded-markup',
  templateUrl: './embedded-markup.component.html',
  styleUrls: ['./embedded-markup.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgStyle, NgClass],
  standalone: true,
})
export class EmbeddedMarkupComponent
  implements OnInit, AfterViewInit, OnDestroy
{
  private readonly changeRef = inject(ChangeDetectorRef);

  readonly iframeRef = viewChild<ElementRef>('iframeElement');

  readonly embed = input<Embeddable>();

  readonly sourceBuilder = input.required<SourceBuilder>();

  readonly maxHeight = input<boolean>(false);

  readonly showBoxes = input<boolean>(false);

  private pickedXpath: EventEmitter<string> = new EventEmitter<string>();

  loadedDocument: () => void;
  iframeRefHeight: number;
  private proxyUrl: string;
  private waitForDocument: Promise<void>;
  private unbindMessageListener: () => void;
  private subscriptions: Subscription[] = [];
  protected currentXpath: string = '';
  protected pickElement: boolean = false;

  constructor() {
    effect(async () => {
      this.assignToIframe();

      if (this.showBoxes()) {
        await this.postIframeMessage({
          id: '',
          type: 'show-boxes',
          data: this.showBoxes(),
        });
      }
    });
  }

  ngOnInit(): void {
    this.waitForDocument = new Promise<void>((resolve) => {
      this.loadedDocument = resolve;
    });
    this.subscriptions.push(
      this.pickedXpath
        .pipe(debounce(() => interval(100)))
        .subscribe((xpath) => {
          this.currentXpath = xpath;
          this.changeRef.detectChanges();
        }),
      this.sourceBuilder().events.extractElements.subscribe((params) => {
        const document = new DOMParser().parseFromString(
          this.embed().data,
          'text/html',
        );
        const xpathResult = document.evaluate(
          params.xpath,
          document,
          null,
          XPathResult.ANY_TYPE,
          null,
        );
        let element = xpathResult.iterateNext();
        const elements: HTMLElement[] = [];
        while (element) {
          elements.push(element as HTMLElement);
          element = xpathResult.iterateNext();
        }
        params.callback(elements);
      }),
      this.sourceBuilder().events.pickElement.subscribe((callback) => {
        console.log('pickElement');
        this.pickElement = true;
        const unsubscribe = this.pickedXpath.subscribe((xpath) => {
          this.pickElement = false;
          unsubscribe.unsubscribe();
          callback(xpath);
        });
      }),
      this.sourceBuilder()
        .events.showElements.pipe(distinct())
        .subscribe((xpath) => {
          console.log(`showElements ${xpath}`);
          this.postIframeMessage({
            id: '',
            type: 'xpath',
            data: xpath,
          });
        }),
    );
  }

  ngOnDestroy(): void {
    if (this.proxyUrl) {
      URL.revokeObjectURL(this.proxyUrl);
    }
    if (this.unbindMessageListener) {
      this.unbindMessageListener();
    }
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async ngAfterViewInit() {
    this.assignToIframe();
    this.changeRef.detectChanges();
  }

  getWidth() {
    if (this.embed()?.viewport) {
      return this.embed().viewport.width + 'px';
    } else {
      return '100%';
    }
  }

  getHeight() {
    if (this.embed()?.viewport) {
      return this.embed().viewport.height + 'px';
    } else {
      if (this.iframeRefHeight) {
        return this.iframeRefHeight + 'px';
      } else {
        return 'auto';
      }
    }
  }

  private postIframeMessage(message: IframeMessage) {
    return this.waitForDocument?.then(() =>
      this.iframeRef().nativeElement.contentWindow?.postMessage(message, '*'),
    );
  }

  private disableClick(document: Document) {
    const head = document.documentElement.querySelector('head');
    head.append(
      new DOMParser().parseFromString(
        `<style>
a, button { pointer-events: none; }
body { cursor: pointer; }
        </style>`,
        'text/html',
      ).documentElement,
    );
  }

  private registerMessageListener() {
    const messageListener = (e: MessageEvent) => {
      const data: IframeMessage = e.data;
      switch (data.type) {
        case 'height':
          if (this.maxHeight()) {
            this.iframeRefHeight = (data.data as number) * 0.7;
            this.changeRef.detectChanges();
          }
          break;
        case 'xpath':
          this.pickedXpath.emit('/' + data.data);
          break;
        case undefined:
          break;
        default:
          console.error(`invalid message type ${data.type}`);
      }
    };
    window.addEventListener('message', messageListener);

    this.unbindMessageListener = () => {
      window.removeEventListener('message', messageListener);
    };
  }

  private patchHtml(html: string, url: string): string {
    const randomId = makeid(10);
    const doc = new DOMParser().parseFromString(html, 'text/html');
    Array.from(doc.querySelectorAll('script')).forEach((el) => el.remove());

    this.registerMessageListener();
    this.disableClick(doc);

    const scriptElement = new DOMParser()
      .parseFromString(
        `<script id="feedless-click-handler" type="application/javascript">
let feedlessBodyHeight = 0;
function postHeightMessage() {
  const height = document.body.scrollHeight;
  if (feedlessBodyHeight !== height) {
    feedlessBodyHeight = height;
    window.parent.postMessage({
        id: '${randomId}',
        type: 'height',
        data: height
      }, '*')
  }
}
setInterval(() => postHeightMessage(), 500);

window.addEventListener('DOMContentLoaded', () => {
    postHeightMessage();
});
document.body.addEventListener('mousedown', (event) => {
  let element = event.target;
  const nodes = [element];
  while(element !== document.body) {
    element = element.parentElement;
    nodes.push(element)
  }
  const pathFromBody = nodes.reverse()
  .map(el => {
    const relatedChildren = Array.from(el?.parentElement?.children || [el])
      .filter(child => child.tagName === el.tagName);
    if (relatedChildren.length > 1) {
      return el.tagName.toLowerCase() + '['+(relatedChildren.indexOf(el)+1)+']';
    } else {
      return el.tagName.toLowerCase();
    }
  })
  .join('/');
  window.parent.postMessage({
      id: '${randomId}',
      type: 'xpath',
      data: 'html/' + pathFromBody
    }, '*')
})

${transformXpathToCssPath.toString()}

window.addEventListener('message', (message) => {
  console.log('iframe message', message.data);
  switch (message.data.type) {
    case 'xpath':
      const cssPath = transformXpathToCssPath(message.data.data);
      document.querySelector('#feedless-style').textContent = cssPath + '{filter: drop-shadow(2px 4px 6px blue);}';
      document.querySelector(cssPath).scrollIntoView()
      break;
    case 'show-boxes':
      if (message.data.data) {
        document.querySelector('#feedless-style').textContent = 'div:hover, article:hover, section:hover { border: 1px dashed blue !important;}';
      }
      break;
  }
})

      </script>`,
        'text/html',
      )
      .querySelector('#feedless-click-handler');

    const styleElement = new DOMParser()
      .parseFromString(`<style id="feedless-style"></style>`, 'text/html')
      .querySelector('#feedless-style');

    const base = doc.createElement('base');
    base.setAttribute('href', url);
    doc.head.prepend(base, styleElement);
    doc.querySelector('body').append(scriptElement);

    Array.from(doc.querySelectorAll('[href]')).forEach((el) => {
      try {
        const absoluteUrl = new URL(el.getAttribute('href'), url).toString();
        el.setAttribute('href', absoluteUrl.toString());
      } catch (e) {
        // console.error(e);
      }
    });
    Array.from(doc.querySelectorAll('[src]')).forEach((el) => {
      try {
        const absoluteUrl = new URL(el.getAttribute('src'), url).toString();
        el.setAttribute('src', absoluteUrl.toString());
      } catch (e) {
        // console.error(e);
      }
    });

    return doc.documentElement.outerHTML;
  }

  private assignToIframe() {
    const document = this.embed();
    const iframe = this.iframeRef();
    if (
      iframe &&
      document &&
      document.mimeType &&
      !document.mimeType?.startsWith('text/xml')
    ) {
      const html = this.patchHtml(this.embed().data, this.embed().url);
      this.proxyUrl = URL.createObjectURL(
        new Blob([html], {
          type: 'text/html;charset=UTF-8',
        }),
      );
      // this.safeBlobUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.proxyUrl);
      iframe.nativeElement.src = this.proxyUrl;
      this.changeRef.detectChanges();
    }
  }
}
