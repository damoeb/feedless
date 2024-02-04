import {
  AfterViewInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges,
  ViewChild,
} from '@angular/core';
import { isDefined } from '../../modals/feed-builder-modal/scrape-builder';

interface Viewport {
  width: number;
  height: number;
}

export interface Embeddable {
  mimeType: string;
  data: string;
  url: string;
  viewport?: Viewport;
}

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
  data: string | number;
}

@Component({
  selector: 'app-embedded-website',
  templateUrl: './embedded-website.component.html',
  styleUrls: ['./embedded-website.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmbeddedWebsiteComponent
  implements OnInit, AfterViewInit, OnChanges, OnDestroy
{
  @ViewChild('iframeElement')
  iframeRef: ElementRef;

  @Input({ required: true })
  embed: Embeddable;

  @Input()
  highlightXpath: string;

  @Input()
  maxHeight: boolean = false;

  @Input()
  showBoxes: boolean = false;

  @Output()
  pickedXpath: EventEmitter<string> = new EventEmitter<string>();

  loadedDocument: () => void;
  iframeRefHeight: number;
  private proxyUrl: string;
  private waitForDocument: Promise<void>;
  private unbindMessageListener: () => void;

  constructor(private readonly changeRef: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.waitForDocument = new Promise<void>((resolve) => {
      this.loadedDocument = resolve;
    });
  }

  ngOnDestroy(): void {
    if (this.proxyUrl) {
      window.URL.revokeObjectURL(this.proxyUrl);
    }
    if (this.unbindMessageListener) {
      this.unbindMessageListener();
    }
  }

  async ngOnChanges(changes: SimpleChanges) {
    if (changes.highlightXpath?.currentValue) {
      this.highlightXpath = changes.highlightXpath.currentValue;
      await this.highlightXpathInIframe(this.highlightXpath);
      this.changeRef.detectChanges();
    }
    if (isDefined(changes.showBoxes?.currentValue)) {
      await this.postIframeMessage({
        id: '',
        type: 'show-boxes',
        data: changes.showBoxes?.currentValue,
      });
      this.changeRef.detectChanges();
    }
    if (
      changes.embed?.currentValue &&
      changes.embed.currentValue.mimeType.toLowerCase().startsWith('text/') &&
      changes.embed?.currentValue?.data != changes.embed?.previousValue?.data
    ) {
      this.embed = changes.embed.currentValue;
      this.changeRef.detectChanges();
      this.assignToIframe();
    }
  }

  async ngAfterViewInit() {
    this.assignToIframe();
    await this.highlightXpathInIframe(this.highlightXpath);
    this.changeRef.detectChanges();
  }

  async highlightXpathInIframe(xpath: string) {
    if (xpath) {
      await this.postIframeMessage({
        id: '',
        type: 'xpath',
        data: xpath,
      });
    }
  }

  getWidth() {
    if (this.embed.viewport) {
      return this.embed.viewport.width + 'px';
    } else {
      return '100%';
    }
  }

  getHeight() {
    if (this.embed.viewport) {
      return this.embed.viewport.height + 'px';
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
      this.iframeRef.nativeElement.contentWindow.postMessage(message, '*'),
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
          if (this.maxHeight) {
            this.iframeRefHeight = (data.data as number) * 0.7;
            this.changeRef.detectChanges();
          }
          break;
        case 'xpath':
          this.pickedXpath.emit('/' + data.data);
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
  console.log(message.data);
  switch (message.data.type) {
    case 'xpath':
      const cssPath = transformXpathToCssPath(message.data.data);
      document.querySelector('#feedless-style').textContent = cssPath + '{border: 2px solid red!important; box-shadow: 0px 0px 6px 2px red;}';
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
    const document = this.embed;
    if (document?.mimeType && !document.mimeType?.startsWith('text/xml')) {
      const html = this.patchHtml(this.embed.data, this.embed.url);
      this.proxyUrl = window.URL.createObjectURL(
        new Blob([html], {
          type: 'text/html;charset=UTF-8',
        }),
      );
      // this.safeBlobUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.proxyUrl);
      this.iframeRef.nativeElement.src = this.proxyUrl;
      this.changeRef.detectChanges();
    }
  }
}
