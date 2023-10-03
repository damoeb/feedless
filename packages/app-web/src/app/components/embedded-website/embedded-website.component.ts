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
import { GqlXyPosition } from '../../../generated/graphql';

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

  @Input()
  embed: Embeddable;

  @Input()
  highlightXpath: string;

  @Output()
  pickedXpath: EventEmitter<string> = new EventEmitter<string>();

  loadedDocument: () => void;
  private proxyUrl: string;
  private waitForDocument: Promise<void>;
  private unbindMessageListener: () => void;
  private clickHandlerDelegate: (event: MouseEvent) => void;

  constructor(private readonly changeDetectorRef: ChangeDetectorRef) {}

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

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.highlightXpath?.currentValue) {
      this.highlightXpath = changes.highlightXpath.currentValue;
      this.highlightXpathInIframe(this.highlightXpath);
      this.changeDetectorRef.detectChanges();
    }
    if (
      changes.embed?.currentValue &&
      changes.embed.currentValue.mimeType.toLowerCase().startsWith('text/') &&
      changes.embed?.currentValue?.data != changes.embed?.previousValue?.data &&
      this.iframeRef
    ) {
      this.assignToIframe();
      this.changeDetectorRef.detectChanges();
    }
  }

  ngAfterViewInit(): void {
    this.assignToIframe();
    this.highlightXpathInIframe(this.highlightXpath);
    this.changeDetectorRef.detectChanges();
  }

  highlightXpathInIframe(xpath: string) {
    this.waitForDocument?.then(() => this.highlightXpathInIframeNow(xpath));
  }

  private highlightXpathInIframeNow(xpath: string) {
    try {
      if (!xpath) {
        return;
      }
      this.iframeRef.nativeElement.contentWindow.postMessage(xpath, '*');
    } catch (e) {
      console.error(e);
    }
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

  private registerMessageListener(randomId: string) {
    const messageListener = (e: MessageEvent) => {
      if (e?.data && e.data.indexOf && e.data.indexOf(randomId) === 0) {
        const xpath =
          '/' + e.data.substring(randomId.length + 1, e.data.length);
        this.pickedXpath.emit(xpath);
        // if (this.clickHandlerDelegate) {
        //   this.clickHandlerDelegate(xpath);
        //   this.clickHandlerDelegate = null;
        // }
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

    this.registerMessageListener(randomId);
    this.disableClick(doc);

    const scriptElement = new DOMParser()
      .parseFromString(
        `<script id="feedless-click-handler" type="application/javascript">
document.body.addEventListener('click', (event) => {
  const nodes = event.composedPath();
  console.log('target', event.target)
  const bodyAt = nodes.indexOf(document.firstElementChild);
  const pathFromBody = nodes.filter((_, index) => index <= bodyAt).reverse()
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
  window.parent.postMessage("${randomId} "+pathFromBody, '*')
})

function highlightXpath(xpath) {
  console.log('highlightXpath', xpath);
  if (typeof xpath === 'string') {
    const cssPath = transformXpathToCssPath(xpath);
    // console.log('cssPath', cssPath);
    document.querySelector('#feedless-style').textContent = cssPath + '{border: 3px solid #3880ff!important; margin: 2px!important; padding: 2px!important; display: inline-block!important;};'
  }
}

window.addEventListener('message', (message) => {
  highlightXpath(message.data);
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
      this.iframeRef.nativeElement.src = this.proxyUrl;
    }
    this.changeDetectorRef.detectChanges();
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
      return 'auto';
    }
  }

  handleClick(event: MouseEvent) {
    if (this.clickHandlerDelegate) {
      this.clickHandlerDelegate(event);
      this.clickHandlerDelegate = null;
    }
  }

  pickPosition(callback: (position: GqlXyPosition) => void) {
    this.clickHandlerDelegate = (event: MouseEvent) => {
      callback({
        x: event.offsetX,
        y: event.offsetY,
      });
    };
  }
}
