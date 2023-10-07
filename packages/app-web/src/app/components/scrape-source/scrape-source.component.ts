import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component, EventEmitter,
  OnDestroy,
  OnInit, Output
} from '@angular/core';
import { ProfileService } from '../../services/profile.service';
import { Subscription } from 'rxjs';
import { Embeddable } from '../embedded-website/embedded-website.component';
import {
  GqlPuppeteerWaitUntil,
  GqlScrapeActionInput, GqlScrapePrerenderInput, GqlScrapeRequestInput, InputMaybe
} from '../../../generated/graphql';
import { ScrapeService } from '../../services/scrape.service';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { fixUrl } from '../../pages/getting-started/getting-started.page';
import { AppSelectOption } from '../select/select.component';

type View = 'screenshot' | 'markup';

interface ScreenResolution {
  name: string;
  width: number;
  height: number;
}

@Component({
  selector: 'app-scrape-source',
  templateUrl: './scrape-source.component.html',
  styleUrls: ['./scrape-source.component.scss'],
changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ScrapeSourceComponent implements OnInit, OnDestroy {

  @Output()
  sourceChanged: EventEmitter<GqlScrapeRequestInput> = new EventEmitter<GqlScrapeRequestInput>();

  private subscriptions: Subscription[] = [];

  isDarkMode: boolean;
  embedMarkup: Embeddable;
  embedScreenshot: Embeddable;

  resolutions: ScreenResolution[] = [
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

  resolution = this.resolutions[0];

  url: string;
  loading = false;
  actions: GqlScrapeActionInput[] = [];
  view: View = 'screenshot';
  pickElementDelegate: (xpath: string) => void;

  totalTime: string;
  render: 'static' | 'render' = 'static';
  screenResolutions: AppSelectOption[];

  constructor(
    readonly profile: ProfileService,
    private readonly changeRef: ChangeDetectorRef,
    private readonly scrapeService: ScrapeService,
  ) {}

  ngOnInit() {
    this.subscriptions.push(
      this.profile.watchColorScheme().subscribe((isDarkMode) => {
        this.isDarkMode = isDarkMode;
        this.changeRef.detectChanges();
      }),
    );
    this.screenResolutions = this.getScreenResolutions()
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

    const response = await this.scrapeService.scrape(this.getScrapeRequest());

    if (response.failed) {
      console.error(response.errorMessage);
    } else {
      this.totalTime =
        (
          (response.debug.metrics.queue + response.debug.metrics.render) /
          1000
        ).toFixed(2) + 's';
      this.view = 'markup';
      console.log('response.debug.contentType', response.debug.contentType);
      this.embedMarkup = {
        mimeType: response.debug.contentType,
        data: response.debug.html,
        url: this.url,
        viewport: response.debug.viewport,
      };
      if (response.debug.screenshot) {
        this.view = 'screenshot';
        this.embedScreenshot = {
          mimeType: 'image/png',
          data: response.debug.screenshot,
          url: this.url,
          viewport: response.debug.viewport,
        };
      }
    }

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
    console.log('actions', JSON.stringify(actions));
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

    if (this.resolution) {
      prerender = {
        waitUntil: GqlPuppeteerWaitUntil.Load,
        viewport: {
          isMobile: false,
          height: this.resolution.height,
          width: this.resolution.width,
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
      emit: [],
      elements: [],
    };
  }

  private getScreenResolutions(): AppSelectOption[] {
    return this.resolutions.map(r => ({
      value: r,
      label: `${r.width}×${r.height} (${r.name})`
    }))
  }
}
