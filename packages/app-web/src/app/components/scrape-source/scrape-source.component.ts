import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { ProfileService } from '../../services/profile.service';
import { Subscription } from 'rxjs';
import { Embeddable } from '../embedded-website/embedded-website.component';
import {
  GqlPuppeteerWaitUntil,
  GqlScrapeActionInput,
  GqlScrapeEmitType,
  GqlScrapePrerenderInput,
  GqlScrapeRequestInput,
  InputMaybe
} from '../../../generated/graphql';
import { ScrapeService } from '../../services/scrape.service';
import { FormControl } from '@angular/forms';
import { fixUrl } from '../../pages/getting-started/getting-started.page';
import { ScrapeResponse } from '../../graphql/types';
import { KeyLabelOption } from '../select/select.component';

type View = 'screenshot' | 'markup';

interface ScreenResolution {
  name: string;
  width: number;
  height: number;
}

type RenderOption = 'static' | 'chrome';

@Component({
  selector: 'app-scrape-source',
  templateUrl: './scrape-source.component.html',
  styleUrls: ['./scrape-source.component.scss'],
changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ScrapeSourceComponent implements OnInit, OnDestroy {

  @Output()
  requestChanged: EventEmitter<GqlScrapeRequestInput> = new EventEmitter<GqlScrapeRequestInput>();

  @Output()
  responseChanged: EventEmitter<ScrapeResponse> = new EventEmitter<ScrapeResponse>();

  @Input()
  scrapeRequest: GqlScrapeRequestInput;

  @Input()
  scrapeResponse: ScrapeResponse;

  private subscriptions: Subscription[] = [];

  isDarkMode: boolean;
  embedMarkup: Embeddable;
  embedScreenshot: Embeddable;

  screenResolutions: ScreenResolution[] = [
    {
      name: 'XGA',
      width: 1024,
      height: 768,
    },
    {
      name: 'HD720',
      width: 1280,
      height: 720,
    },
    {
      name: 'WXGA',
      width: 1280,
      height: 800,
    },
    {
      name: 'SXGA',
      width: 1280,
      height: 1024,
    },
  ];

  screenResolution = this.screenResolutions[0];

  url: string;
  loading = false;
  actions: GqlScrapeActionInput[] = [];
  view: View = 'screenshot';
  pickElementDelegate: (xpath: string) => void;

  totalTime: string;
  render: RenderOption = 'static';
  renderOptions: KeyLabelOption<RenderOption>[] = [
    {key: 'static', label: 'Static Response', default: true},
    {key: 'chrome', label: 'Headless Chrome'}
  ];

  constructor(
    readonly profile: ProfileService,
    private readonly changeRef: ChangeDetectorRef,
    private readonly scrapeService: ScrapeService,
  ) {}

  ngOnInit() {
    if (this.scrapeRequest) {
      this.url = this.scrapeRequest.page.url;
      this.render = this.scrapeRequest.page.prerender ? 'chrome' : 'static';
      this.actions = this.scrapeRequest.page.actions || [];
      this.changeRef.detectChanges();
    }
    if (this.scrapeResponse) {
      this.handleScrapeResponse(this.scrapeResponse);
      this.changeRef.detectChanges();
    }
    this.subscriptions.push(
      this.profile.watchColorScheme().subscribe((isDarkMode) => {
        this.isDarkMode = isDarkMode;
        this.changeRef.detectChanges();
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async scrapeUrl() {
    this.url = fixUrl(this.url);
    this.changeRef.detectChanges();

    if (this.loading) {
      return;
    }

    this.loading = true;
    this.changeRef.detectChanges();

    const scrapeRequest = this.getScrapeRequest();
    this.requestChanged.emit(scrapeRequest);

    const scrapeResponse = await this.scrapeService.scrape(scrapeRequest);
    this.handleScrapeResponse(scrapeResponse);

    this.loading = false;
    this.changeRef.detectChanges();
  }

  handlePickedXpath(xpath: string) {
    if (this.pickElementDelegate) {
      this.pickElementDelegate(xpath);
      this.pickElementDelegate = null;
    }
  }

  async handleActionChanged(actions: GqlScrapeActionInput[]) {
    this.actions = actions;
    // await this.scrapeUrl();
  }

  registerPickElementDelegate(callback: (xpath: string) => void) {
    this.view = 'markup';
    this.pickElementDelegate = callback;
  }

  pickElement(fc: FormControl<string | null>) {
    this.registerPickElementDelegate((xpath) => fc.setValue(xpath));
  }

  private getScrapeRequest(): GqlScrapeRequestInput {

    let prerender: InputMaybe<GqlScrapePrerenderInput>;

    if (this.render === 'chrome') {
      prerender = {
        waitUntil: GqlPuppeteerWaitUntil.Load,
        viewport: {
          isMobile: false,
          height: this.screenResolution.height,
          width: this.screenResolution.width,
        },
      }
    }

    return {
      page: {
        url: this.url,
        actions: this.actions,
        prerender,
      },
      debug: {
        screenshot: true,
        console: true,
        cookies: true,
        html: true,
      },
      emit: [GqlScrapeEmitType.Feeds],
      elements: ['/'],
    };
  }

  private handleScrapeResponse(scrapeResponse: ScrapeResponse) {
    if (scrapeResponse.failed) {
      console.error(scrapeResponse.errorMessage);
    } else {
      this.responseChanged.emit(scrapeResponse);
      this.totalTime =
        (
          (scrapeResponse.debug.metrics.queue + scrapeResponse.debug.metrics.render) /
          1000
        ).toFixed(2) + 's';
      this.view = 'markup';
      console.log('response.debug.contentType', scrapeResponse.debug.contentType);
      this.embedMarkup = {
        mimeType: scrapeResponse.debug.contentType,
        data: scrapeResponse.debug.html,
        url: this.url,
        viewport: scrapeResponse.debug.viewport,
      };
      if (scrapeResponse.debug.screenshot) {
        this.view = 'screenshot';
        this.embedScreenshot = {
          mimeType: 'image/png',
          data: scrapeResponse.debug.screenshot,
          url: this.url,
          viewport: scrapeResponse.debug.viewport,
        };
      }
    }
  }

  screenResolutionLabelProvider(sr: ScreenResolution): string {
    return `${sr.name} ${sr.width}x${sr.height}`;
  }
}
