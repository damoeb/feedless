import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  ViewChild,
  ViewEncapsulation,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { refresh } from 'ionicons/icons';
import { findScrapeDataByType } from '../../components/reader/reader.component';
import { ScrapeService } from '../../services/scrape.service';
import { GqlScrapeEmitType } from '../../../generated/graphql';
import {
  BasicContent,
  ScrapedReadability,
  ScrapeResponse,
  Selectors,
} from '../../graphql/types';
import {
  EmbedWebsite,
  transformXpathToCssPath,
} from '../../components/embedded-website/embedded-website.component';
import { uniqBy } from 'lodash-es';
import { ProfileService } from '../../services/profile.service';
import { ModalController } from '@ionic/angular';
import { Maybe } from 'graphql/jsutils/Maybe';

type InlineContent = Pick<BasicContent, 'title' | 'url' | 'contentText'> & {
  hostname: string;
};

export type ReaderTextTransform = 'normal' | 'bionic';
export type ReaderLinkTarget = 'reader' | 'blank';

export interface ReaderOptions {
  url: string;
  font: 'serif' | 'sans-serif';
  textAlignment: 'left' | 'justify';
  textTransform: ReaderTextTransform;
  fontSize: number;
  letterSpacing: number;
  contentWidth: number;
  lineHeight: number;
  linkTarget: ReaderLinkTarget;
  verboseLink: boolean;
}

@Component({
  selector: 'app-reader-page',
  templateUrl: './reader.page.html',
  styleUrls: ['./reader.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None,
})
export class ReaderPage implements OnInit, OnDestroy {
  url: string;
  output: 'website' | 'list' | 'article' = 'article';
  private subscriptions: Subscription[] = [];
  protected readonly refresh = refresh;

  @ViewChild('readerContent')
  readerContent: ElementRef;

  readerOptions: ReaderOptions = {
    url: '',
    font: 'serif',
    linkTarget: 'reader',
    contentWidth: 700,
    letterSpacing: 0.05,
    textAlignment: 'left',
    textTransform: 'normal',
    fontSize: 18,
    verboseLink: true,
    lineHeight: 2,
  };

  isDarkMode: boolean;
  contentWidthStepSize: number = 20;

  scrapeResponse: ScrapeResponse;
  embedWebsite: EmbedWebsite;
  groupsOfArticles: InlineContent[][] = [];
  progress = 0;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly scrapeService: ScrapeService,
    readonly profile: ProfileService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  ngOnInit() {
    this.subscriptions.push(
      this.activatedRoute.queryParams.subscribe(async (params) => {
        this.url = params.url;
        await this.scrapeUrl();
      }),
      this.profile.watchColorScheme().subscribe((isDarkMode) => {
        this.isDarkMode = isDarkMode;
        this.changeRef.detectChanges();
      }),
    );
  }

  async scrapeUrl() {
    this.scrapeResponse = await this.scrapeService.scrape({
      page: {
        url: this.url,
      },
      emit: [
        GqlScrapeEmitType.Markup,
        GqlScrapeEmitType.Feeds,
        GqlScrapeEmitType.Readability,
      ],
      elements: ['/'],
    });

    this.embedWebsite = {
      mimeType: 'text/html',
      htmlBody: this.scrapeResponse.elements[0].data.find(
        (it) => it.type === GqlScrapeEmitType.Markup,
      ).markup,
      url: this.url,
    };

    this.groupsOfArticles = this.parseArticles();

    this.changeRef.detectChanges();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async triggerUpdate() {
    await this.scrapeUrl();
  }

  parseArticles(): InlineContent[][] {
    if (this.scrapeResponse) {
      const data = this.scrapeResponse.elements[0].data;
      const selectors: Selectors[] = uniqBy(
        data
          .find((it) => it.type === GqlScrapeEmitType.Feeds)
          .feeds.genericFeeds.map((it) => it.selectors),
        'contextXPath',
      );

      const { markup } = data.find(
        (it) => it.type === GqlScrapeEmitType.Markup,
      );

      const document = new DOMParser().parseFromString(markup, 'text/html');

      return selectors
        .map((it) =>
          Array.from(
            document.querySelectorAll(transformXpathToCssPath(it.contextXPath)),
          )
            .map<InlineContent>((context) => {
              const linkElement = context.querySelector(
                transformXpathToCssPath(it.linkXPath),
              );
              if (linkElement) {
                const url = linkElement.getAttribute('href');
                const { hostname } = new URL(url, this.url);
                return {
                  contentText: context.textContent,
                  url,
                  title: linkElement?.textContent,
                  hostname,
                };
              }
            })
            .filter(
              (content) =>
                content && content.contentText && content.url && content.title,
            ),
        )
        .filter((group) => group.length > 0);
    }
    return [];
  }

  changeOption<T extends keyof ReaderOptions, V extends ReaderOptions[T]>(
    option: T,
    value: V,
  ) {
    this.readerOptions[option] = value;
    this.changeRef.detectChanges();
  }

  changeNumOption<
    T extends keyof Pick<
      ReaderOptions,
      'fontSize' | 'contentWidth' | 'lineHeight'
    >,
    V extends ReaderOptions[T],
  >(
    numOption: T,
    increment: number,
    constraints: { min: number; max: number },
  ) {
    this.changeOption(
      numOption,
      parseFloat(
        Math.max(
          Math.min(this.readerOptions[numOption] + increment, constraints.max),
          constraints.min,
        ).toFixed(1),
      ),
    );
  }

  getCssVariable<T extends keyof ReaderOptions>(key: T) {
    switch (key) {
      case 'font':
        if (this.readerOptions[key] == 'serif') {
          return '--font-serif';
        } else {
          return '--font-sans-serif';
        }
      case 'fontSize':
        return `${this.readerOptions[key]}px`;
      case 'lineHeight':
        return `${this.readerOptions[key]}rem`;
      case 'contentWidth':
        return `${this.readerOptions[key]}px`;
      case 'letterSpacing':
        return `${this.readerOptions[key]}em`;
      case 'textAlignment':
        return this.readerOptions[key];
    }
  }

  pinFormatter(format: string) {
    return (value: number) => `${value}${format}`;
  }

  handleScroll(event: any) {
    this.progress = parseFloat(
      (
        (100 * event.detail.scrollTop) /
        (this.readerContent.nativeElement.scrollHeight -
          document.defaultView.innerHeight)
      ).toFixed(1),
    );
    this.changeRef.detectChanges();
  }

  ifActiveOption<T extends keyof ReaderOptions, V extends ReaderOptions[T]>(
    option: T,
    value: V,
  ) {
    if (this.readerOptions[option] == value) {
      return 'primary';
    }
    return 'light';
  }
  getReadability(): Maybe<ScrapedReadability> {
    return (
      findScrapeDataByType(GqlScrapeEmitType.Readability, this.scrapeResponse)
        ?.readability || {}
    );
  }
}
