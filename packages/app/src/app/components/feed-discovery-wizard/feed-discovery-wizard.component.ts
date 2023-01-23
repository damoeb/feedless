import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnInit,
  Output,
  ViewChild
} from '@angular/core';
import { FeedDiscoveryResult, FeedService, GenericFeed, TransientGenericFeed, TransientNativeFeed } from '../../services/feed.service';
import { ActivatedRoute, Router } from '@angular/router';
import { GqlExtendContentOptions, GqlFetchOptionsInput, GqlParserOptionsInput, GqlPuppeteerWaitUntil } from '../../../generated/graphql';
import { cloneDeep, find, omit } from 'lodash';
import { WebToFeedParams } from '../api-params';
import { ModalDismissal } from '../../app.module';
import { PreviewFeedModalComponent, PreviewFeedModalComponentProps } from '../preview-feed-modal/preview-feed-modal.component';
import { ModalController } from '@ionic/angular';

type FeedParserOptions = GqlParserOptionsInput;
type FeedFetchOptions = GqlFetchOptionsInput;

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

export interface LabelledSelectOption {
  value: string;
  label: string;
}

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

  @Input()
  genericFeed: GenericFeed;

  @Input()
  saveLabelPrefix = 'Use';

  @Output()
  chooseGeneric: EventEmitter<TransientGenericFeedAndDiscovery> = new EventEmitter<TransientGenericFeedAndDiscovery>();

  @Output()
  chooseNative: EventEmitter<TransientNativeFeedAndDiscovery> = new EventEmitter<TransientNativeFeedAndDiscovery>();

  discovery: FeedDiscoveryResult;

  currentGenericFeed: TransientGenericFeed;
  currentNativeFeed: TransientNativeFeed;

  parserOptions: FeedParserOptions = {
    strictMode: false,
    eventFeed: false,
  };

  fetchOptions: FeedFetchOptions = {
    prerender: false,
    prerenderWaitUntil: GqlPuppeteerWaitUntil.Load,
    prerenderWithoutMedia: false,
    websiteUrl: '',
  };

  latLon: string;
  private proxyUrl: string;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly feedService: FeedService,
    private readonly modalCtrl: ModalController,
    private readonly router: Router,
    private readonly changeDetectorRef: ChangeDetectorRef
  ) {}

  async ngOnInit() {
    if (this.genericFeed) {
      this.fetchOptions = omit(
        this.genericFeed.specification.fetchOptions,
        '__typename'
      );
    }
    if (this.url) {
      this.fetchOptions.websiteUrl = this.url;
    }
    if (this.fetchOptions.websiteUrl) {
      await this.fetchDiscovery();
    }
    this.activatedRoute.queryParams.subscribe((params) => {
      if (params.wait) {
        this.fetchOptions.prerenderWaitUntil = params.wait;
      }
      if (params.prerender) {
        this.fetchOptions.prerender = params.prerender;
      }
      if (params.url) {
        this.fetchOptions.websiteUrl = params.url;
        this.fetchDiscovery();
      }
      this.changeDetectorRef.detectChanges();
    });
  }

  pickGenericFeed(genericFeed: TransientGenericFeed) {
    this.currentGenericFeed = cloneDeep(genericFeed);
    const iframeDocument = this.iframeRef.nativeElement.contentDocument;
    const id = 'rss-proxy-style';

    try {
      iframeDocument.getElementById(id).remove();
    } catch (e) {}
    const styleNode = iframeDocument.createElement('style');
    styleNode.setAttribute('type', 'text/css');
    styleNode.setAttribute('id', id);
    const allMatches: HTMLElement[] = this.evaluateXPathInIframe(
      genericFeed.selectors.contextXPath,
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
    // console.log(cssSelectorContextPath);
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
      queryParamsHandling: 'merge',
      queryParams: { url: this.fetchOptions.websiteUrl },
      relativeTo: this.activatedRoute,
    });

    await this.router.navigateByUrl(urlTree, { replaceUrl: true });
    // this.discovery = null;
    this.discovery = await this.feedService.discoverFeeds({
      parserOptions: this.parserOptions,
      fetchOptions: this.fetchOptions,
    });

    if (this.genericFeed) {
      this.currentGenericFeed = find(this.discovery.genericFeeds.feeds, {
        hash: this.genericFeed.hash,
      });
    }

    this.assignToIframe();
    this.changeDetectorRef.detectChanges();
  }

  async pushQueryParam(paramName: string, value: string | number | boolean) {
    const urlTree = this.router.createUrlTree([], {
      queryParamsHandling: 'merge',
      queryParams: { [paramName]: value },
      relativeTo: this.activatedRoute,
    });
    await this.router.navigateByUrl(urlTree, { replaceUrl: true });
  }

  async previewCurrentFeed() {
    const feedUrl = this.getCurrentFeedUrl();
    const componentProps: PreviewFeedModalComponentProps = {
      feedUrl,
    };
    const modal = await this.modalCtrl.create({
      component: PreviewFeedModalComponent,
      componentProps,
      backdropDismiss: false,
    });
    await modal.present();
    await modal.onDidDismiss<ModalDismissal>();
  }

  pickNativeFeed(nativeFeed: TransientNativeFeed) {
    this.currentGenericFeed = null;
    this.currentNativeFeed = nativeFeed;
  }

  useCurrentGenericFeed() {
    this.chooseGeneric.emit([this.currentGenericFeed, this.discovery]);
  }

  useCurrentNativeFeed() {
    this.chooseNative.emit([this.currentNativeFeed, this.discovery]);
  }

  getExtendContextOptions(): LabelledSelectOption[] {
    return Object.values(GqlExtendContentOptions).map((option) => ({
      label: option,
      value: option,
    }));
  }

  getPrerenderWaitUntilOptions(): LabelledSelectOption[] {
    return Object.values(GqlPuppeteerWaitUntil).map((option) => ({
      label: option.toLowerCase(),
      value: option,
    }));
  }

  private getCurrentFeedUrl(): string {
    if (this.currentGenericFeed) {
      const str = (value: boolean | number): string => `${value}`;

      const url = new URL(this.currentGenericFeed.feedUrl);
      const searchParams = url.searchParams;
      const selectors = this.currentGenericFeed.selectors;
      searchParams.set(WebToFeedParams.contextPath, selectors.contextXPath);
      searchParams.set(WebToFeedParams.datePath, selectors.dateXPath);
      searchParams.set(WebToFeedParams.linkPath, selectors.linkXPath);
      searchParams.set(
        WebToFeedParams.extendContent,
        this.toExtendContextParam(selectors.extendContext)
      );
      searchParams.set(
        WebToFeedParams.prerender,
        str(this.fetchOptions.prerender)
      );
      searchParams.set(
        WebToFeedParams.strictMode,
        str(this.parserOptions.strictMode)
      );
      searchParams.set(
        WebToFeedParams.eventFeed,
        str(this.parserOptions.eventFeed)
      );
      searchParams.set(
        WebToFeedParams.prerenderWaitUntil,
        this.fetchOptions.prerenderWaitUntil
      );
      this.currentGenericFeed.feedUrl = url.toString();
      return this.currentGenericFeed.feedUrl;
    } else {
      return this.currentNativeFeed.url;
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

    return doc;
  }

  private assignToIframe() {
    const document = this.discovery?.document;
    if (document?.mimeType && !document.mimeType?.startsWith('text/xml')) {
      const html = this.patchHtml(
        document.htmlBody,
        this.fetchOptions.websiteUrl
      ).documentElement.innerHTML;
      this.proxyUrl = window.URL.createObjectURL(
        new Blob([html], {
          type: 'text/html',
        })
      );
      this.iframeRef.nativeElement.src = this.proxyUrl;
    }
    this.changeDetectorRef.detectChanges();
  }

  private toExtendContextParam(extendContext: GqlExtendContentOptions): string {
    switch (extendContext) {
      case GqlExtendContentOptions.PreviousAndNext:
        return 'pn';
      default:
        return extendContext.toString()[0].toLowerCase();
    }
  }
}
