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
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { refresh } from 'ionicons/icons';
import { ScrapeService } from '../../services/scrape.service';
import {
  GqlFeedlessPlugins,
  GqlScrapedFeeds,
} from '../../../generated/graphql';
import {
  ScrapedReadability,
  ScrapeResponse,
  Selectors,
} from '../../graphql/types';
import {
  Embeddable,
  transformXpathToCssPath,
} from '../../components/embedded-website/embedded-website.component';
import { uniqBy } from 'lodash-es';
import { ProfileService } from '../../services/profile.service';
import { Maybe } from 'graphql/jsutils/Maybe';
import {
  fixUrl,
  isValidUrl,
} from '../../pages/getting-started/getting-started.page';

type InlineContent = {
  hostname: string;
  contentTitle: string;
  url: string;
  contentText: string;
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
  embedWebsite: Embeddable;
  groupsOfArticles: InlineContent[][] = [];
  progress = 0;
  loading = false;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly scrapeService: ScrapeService,
    private readonly router: Router,
    readonly profile: ProfileService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async ngOnInit() {
    const urlInParams = this.activatedRoute.snapshot.params.url;
    if (urlInParams?.length > 0) {
      await this.assignUrlQueryParam(urlInParams);
    } else {
      this.subscriptions.push(
        this.activatedRoute.queryParams.subscribe(async (params) => {
          if (params.url?.length > 0) {
            this.url = fixUrl(params.url);
            await this.scrapeUrl();
          }
        }),
        this.profile.watchColorScheme().subscribe((isDarkMode) => {
          this.isDarkMode = isDarkMode;
          this.changeRef.detectChanges();
        }),
      );
    }
  }

  private async assignUrlQueryParam(url: string) {
    await this.router.navigate(['/'], {
      replaceUrl: true,
      queryParams: {
        url: fixUrl(url),
      },
    });
  }

  async scrapeUrl() {
    if (!this.url) {
      return;
    }
    if (!isValidUrl(this.url)) {
      this.url = fixUrl(this.url);
    }
    this.assignUrlQueryParam(this.url);
    console.log(`scrape ${this.url}`);
    this.loading = true;
    this.changeRef.detectChanges();

    this.scrapeResponse = await this.scrapeService.scrape({
      page: {
        url: this.url,
      },
      emit: [
        {
          selectorBased: {
            xpath: {
              value: '/',
            },
            expose: {
              transformers: [
                {
                  pluginId: GqlFeedlessPlugins.OrgFeedlessFulltext,
                },
                {
                  pluginId: GqlFeedlessPlugins.OrgFeedlessFeeds,
                },
              ],
            },
          },
        },
      ],
    });

    this.embedWebsite = {
      mimeType: 'text/html',
      data: this.scrapeResponse.elements[0].selector.html.data,
      url: this.url,
    };

    this.groupsOfArticles = this.parseArticles();

    this.loading = false;
    this.changeRef.detectChanges();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async triggerUpdate() {
    if (!this.loading) {
      await this.scrapeUrl();
    }
  }

  parseArticles(): InlineContent[][] {
    if (this.scrapeResponse) {
      const data = this.scrapeResponse.elements[0].selector;
      const feeds = JSON.parse(
        data.fields.find(
          (field) => field.name === GqlFeedlessPlugins.OrgFeedlessFeeds,
        ).value.one.data,
      ) as GqlScrapedFeeds;

      const selectors: Selectors[] = uniqBy(
        feeds.genericFeeds.map((it) => it.selectors),
        'contextXPath',
      );

      const raw = data.html.data;

      const document = new DOMParser().parseFromString(raw, 'text/html');

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
                  contentTitle: linkElement?.textContent,
                  hostname,
                };
              }
            })
            .filter(
              (content) =>
                content &&
                content.contentText &&
                content.url &&
                content.contentTitle,
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
    return JSON.parse(
      this.scrapeResponse.elements[0].selector.fields.find(
        (field) => field.name === GqlFeedlessPlugins.OrgFeedlessFulltext,
      ).value.one.data,
    ) as ScrapedReadability;
  }
}
