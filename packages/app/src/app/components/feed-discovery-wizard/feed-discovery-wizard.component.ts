import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnInit,
  Output,
  ViewChild,
} from '@angular/core';
import {
  FeedDiscoveryResult,
  FeedService,
  TransientGenericFeed,
  TransientNativeFeed,
} from '../../services/feed.service';
import { ActivatedRoute, Router } from '@angular/router';

interface ArticleCandidate {
  elem: HTMLElement;
  index: number;
  qualified: boolean;
}

export type TransientGenericFeedAndDiscovery = [
  TransientGenericFeed,
  FeedDiscoveryResult
];
export type TransientNativeFeedAndDiscovery = [
  TransientNativeFeed,
  FeedDiscoveryResult
];

@Component({
  selector: 'app-feed-discovery-wizard',
  templateUrl: './feed-discovery-wizard.component.html',
  styleUrls: ['./feed-discovery-wizard.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeedDiscoveryWizardComponent implements OnInit {
  @ViewChild('iframeElement')
  iframeRef: ElementRef;

  @Input()
  url: string;

  @Output()
  chooseGeneric: EventEmitter<TransientGenericFeedAndDiscovery> = new EventEmitter<TransientGenericFeedAndDiscovery>();

  @Output()
  chooseNative: EventEmitter<TransientNativeFeedAndDiscovery> = new EventEmitter<TransientNativeFeedAndDiscovery>();

  discovery: FeedDiscoveryResult;

  currentGenericFeed: TransientGenericFeed;
  private proxyUrl: string;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly feedService: FeedService,
    private readonly router: Router,
    private readonly changeDetectorRef: ChangeDetectorRef
  ) {}

  async ngOnInit() {
    this.activatedRoute.queryParams.subscribe((params) => {
      if (params.url) {
        this.url = params.url;
        this.fetchDiscovery();
      }
    });
    if (this.url) {
      await this.fetchDiscovery();
    }
  }

  highlightGenericFeed(genericFeed: TransientGenericFeed) {
    this.currentGenericFeed = genericFeed;
    const iframeDocument = this.iframeRef.nativeElement.contentDocument;
    const id = 'rss-proxy-style';

    try {
      iframeDocument.getElementById(id).remove();
    } catch (e) {}
    const styleNode = iframeDocument.createElement('style');
    styleNode.setAttribute('type', 'text/css');
    styleNode.setAttribute('id', id);
    const allMatches: HTMLElement[] = this.evaluateXPathInIframe(
      genericFeed.contextXPath,
      iframeDocument
    );

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
    console.log(cssSelectorContextPath);
    const code = `${matchingIndexes
      .map((index) => `${cssSelectorContextPath}:nth-child(${index + 1})`)
      .join(', ')} {
            border: 3px solid blue!important;
            margin: 2px!important;
            padding: 2px!important;
            display: block;
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
  }

  async fetchDiscovery() {
    const urlTree = this.router.createUrlTree([], {
      queryParams: { url: this.url },
      relativeTo: this.activatedRoute,
    });
    await this.router.navigateByUrl(urlTree, { replaceUrl: true });
    this.discovery = await this.feedService.discoverFeeds(this.url);
    this.assignToIframe();
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

    return doc;
  }

  private assignToIframe() {
    if (
      this.discovery?.mimeType &&
      !this.discovery.mimeType?.startsWith('text/xml')
    ) {
      const html = this.patchHtml(this.discovery.htmlBody, this.url)
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
