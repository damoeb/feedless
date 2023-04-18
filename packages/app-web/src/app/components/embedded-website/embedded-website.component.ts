import {
  AfterViewInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  SimpleChanges,
  ViewChild,
} from '@angular/core';

interface ArticleCandidate {
  elem: HTMLElement;
  index: number;
  qualified: boolean;
}

export interface EmbedWebsite {
  mimeType: string;
  htmlBody: string;
  url: string;
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
  document: EmbedWebsite;

  @Input()
  highlightXpath: string;

  loadedDocument: () => void;
  private proxyUrl: string;
  private waitForDocument: Promise<void>;

  constructor(private readonly changeDetectorRef: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.waitForDocument = new Promise<void>(
      (resolve) => (this.loadedDocument = resolve)
    );
  }

  ngOnDestroy(): void {
    if (this.proxyUrl) {
      window.URL.revokeObjectURL(this.proxyUrl);
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.highlightXpath?.currentValue) {
      this.highlightXpath = changes.highlightXpath.currentValue;
      this.highlightXpathInIframe();
      this.changeDetectorRef.detectChanges();
    }
    if (changes.document?.currentValue && this.iframeRef) {
      this.assignToIframe();
      this.changeDetectorRef.detectChanges();
    }
  }

  ngAfterViewInit(): void {
    this.assignToIframe();
    this.highlightXpathInIframe();
    this.changeDetectorRef.detectChanges();
  }

  highlightXpathInIframe() {
    this.waitForDocument?.then(() => this.highlightXpathInIframeNow());
  }

  private highlightXpathInIframeNow() {
    try {
      if (!this.highlightXpath) {
        return;
      }
      const iframeDocument = this.iframeRef.nativeElement.contentDocument;
      const id = 'rss-proxy-style';

      try {
        iframeDocument.getElementById(id).remove();
      } catch (e) {}
      const styleNode = iframeDocument.createElement('style');
      styleNode.setAttribute('type', 'text/css');
      styleNode.setAttribute('id', id);
      const allMatches: HTMLElement[] = this.evaluateXPathInIframe(
        this.highlightXpath,
        iframeDocument
      ).filter((match) => match);

      const matchingIndexes = allMatches
        .map((elem) => {
          const index = Array.from(elem.parentElement.children).findIndex(
            (otherElem) => otherElem === elem
          );
          // const qualified = true;
          // if (qualified) {
          //   console.log(`Keeping element ${index}`, elem);
          // } else {
          //   console.log(`Removing unqualified element ${index}`, elem);
          // }
          return { elem, index } as ArticleCandidate;
        })
        .map((candidate) => candidate.index);

      const cssSelectorContextPath =
        'body>' +
        this.getRelativeCssPath(allMatches[0], iframeDocument.body, false);
      // console.log(cssSelectorContextPath);
      const code = `${matchingIndexes
        .map((index) => `${cssSelectorContextPath}:nth-child(${index + 1})`)
        .join(', ')} {
              border: 3px solid #3880ff!important;
              margin: 2px!important;
              padding: 2px!important;
              display: inline-block!important;
            }
            `;

      styleNode.appendChild(iframeDocument.createTextNode(code));
      const existingStyleNode = iframeDocument.head.querySelector(`#${id}`);
      if (existingStyleNode) {
        existingStyleNode.remove();
      }
      iframeDocument.head.appendChild(styleNode);
      setTimeout(() => {
        const firstMatch = allMatches[0];
        if (firstMatch) {
          firstMatch.scrollIntoView({ behavior: 'smooth' });
        }
      }, 500);
    } catch (e) {
      console.error(e);
    }
  }

  private evaluateXPathInIframe(
    xPath: string,
    context: HTMLElement | Document
  ): HTMLElement[] {
    const iframeDocument = this.iframeRef.nativeElement.contentDocument;
    const xpathResult = iframeDocument.evaluate(xPath, context, null, 5);
    const nodes: HTMLElement[] = [];
    let node = xpathResult.iterateNext();
    while (node) {
      nodes.push(node as HTMLElement);
      node = xpathResult.iterateNext();
    }
    return nodes;
  }

  private getRelativeCssPath(
    node: HTMLElement,
    context: HTMLElement,
    withClassNames = false
  ): string {
    if (node.nodeType === 3 || node === context) {
      // todo mag this is not applicable
      return 'self';
    }
    let path = node.tagName; // tagName for text nodes is undefined
    while (node.parentNode !== context) {
      node = node.parentNode as HTMLElement;
      if (typeof path === 'undefined') {
        path = this.getTagName(node, withClassNames);
      } else {
        path = `${this.getTagName(node, withClassNames)}>${path}`;
      }
    }
    return path;
  }

  private getTagName(node: HTMLElement, withClassNames: boolean): string {
    if (!withClassNames) {
      return node.tagName;
    }
    const classList = Array.from(node.classList).filter(
      (cn) => cn.match('[0-9]+') === null
    );
    if (classList.length > 0) {
      return `${node.tagName}.${classList.join('.')}`;
    }
    return node.tagName;
  }

  private patchHtml(html: string, url: string): Document {
    const doc = new DOMParser().parseFromString(html, 'text/html');

    const base = doc.createElement('base');
    base.setAttribute('href', url);
    doc.getElementsByTagName('head').item(0).appendChild(base);

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

    return doc;
  }

  private assignToIframe() {
    const document = this.document;
    if (document?.mimeType && !document.mimeType?.startsWith('text/xml')) {
      const html = this.patchHtml(this.document.htmlBody, this.document.url)
        .documentElement.innerHTML;
      this.proxyUrl = window.URL.createObjectURL(
        new Blob([html], {
          type: 'text/html',
        })
      );
      this.iframeRef.nativeElement.src = this.proxyUrl;
    }
    this.changeDetectorRef.detectChanges();
  }
}
