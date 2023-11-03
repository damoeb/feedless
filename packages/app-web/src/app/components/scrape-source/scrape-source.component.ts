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
  GqlXyPosition,
  InputMaybe
} from '../../../generated/graphql';
import { ScrapeService } from '../../services/scrape.service';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { fixUrl, isValidUrl } from '../../pages/getting-started/getting-started.page';
import { ScrapeResponse } from '../../graphql/types';
import { KeyLabelOption } from '../select/select.component';
import { BoundingBox, XyPosition } from '../embedded-image/embedded-image.component';
import { isDefined } from '../../modals/feed-builder-modal/scrape-builder';

type View = 'screenshot' | 'markup';

interface ScreenResolution {
  name: string;
  width: number;
  height: number;
}

type RenderEngine = 'static' | 'chrome';

type BasicSourceForm = {
  url: FormControl<string>,
  renderEngine: FormControl<RenderEngine>,
  screenResolution: FormControl<ScreenResolution>
};

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

  @Input()
  pickFragment: boolean = false;

  formGroup: FormGroup<BasicSourceForm>;

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

  loading = false;
  actions: GqlScrapeActionInput[] = [];
  view: View = 'screenshot';
  pickElementDelegate: (xpath: string | null) => void;
  pickPositionDelegate: (position: GqlXyPosition | null) => void;
  pickBoundingBoxDelegate: (boundingBox: BoundingBox | null) => void;

  protected readonly isDefined = isDefined;

  totalTime: string;
  renderOptions: KeyLabelOption<RenderEngine>[] = [
    {key: 'static', label: 'Static Response', default: true},
    {key: 'chrome', label: 'Headless Browser'}
  ];
  errorMessage: string;
  highlightXpath: string;
  isFullscreenMode: boolean = false;

  constructor(
    readonly profile: ProfileService,
    private readonly changeRef: ChangeDetectorRef,
    private readonly scrapeService: ScrapeService,
  ) {}

  ngOnInit() {
    this.formGroup = new FormGroup<BasicSourceForm>({
      url: new FormControl<RenderEngine>('static', [Validators.required, Validators.minLength(4)]),
      renderEngine: new FormControl<RenderEngine>('static', Validators.required),
      screenResolution: new FormControl<ScreenResolution>(this.screenResolutions[0], Validators.required)
    });

    if (this.scrapeRequest) {
      this.formGroup.setValue({
        url: this.scrapeRequest.page.url,
        renderEngine: this.scrapeRequest.page.prerender ? 'chrome' : 'static',
        screenResolution: this.screenResolutions[0]
      });
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
      this.formGroup.valueChanges
        .subscribe(values => {
        console.log(values);
        return this.scrapeUrl();
      })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async scrapeUrl() {
    try {
      if (!isValidUrl(this.formGroup.value.url)) {
        this.formGroup.controls.url.setValue(fixUrl(this.formGroup.value.url));
      }
      this.changeRef.detectChanges();

      if (this.loading) {
        return;
      }

      this.errorMessage = null;
      this.loading = true;
      this.changeRef.detectChanges();

      const scrapeRequest = this.getScrapeRequest();
      this.requestChanged.emit(scrapeRequest);

      const scrapeResponse = await this.scrapeService.scrape(scrapeRequest);
      this.handleScrapeResponse(scrapeResponse);

    } catch (e) {
      this.errorMessage = e.message;
    }
    this.loading = false;
    this.changeRef.detectChanges();
  }

  handlePickedXpath(xpath: string) {
    if (this.pickElementDelegate) {
      this.highlightXpath = xpath;
      this.pickElementDelegate(xpath);
      this.pickElementDelegate = null;
    }
  }

  handlePickedPosition(position: XyPosition | null) {
    if (this.pickPositionDelegate) {
      this.pickPositionDelegate(position);
      this.pickPositionDelegate = null;
      this.changeRef.detectChanges();
    }
  }

  handlePickedBoundingBox(boundingBox: BoundingBox | null) {
    if (this.pickBoundingBoxDelegate) {
      this.pickBoundingBoxDelegate(boundingBox);
      this.pickBoundingBoxDelegate = null;
      this.changeRef.detectChanges();
    }
  }

  async handleActionChanged(actions: GqlScrapeActionInput[]) {
    console.log('actions changed');
    this.actions = actions;
    // await this.scrapeUrl();
  }

  registerPickElementDelegate(callback: (xpath: string | null) => void) {
    this.view = 'markup';
    this.isFullscreenMode = true;
    this.pickElementDelegate = (xpath: string | null) => {
      this.isFullscreenMode = false;
      callback(xpath);
    }
  }

  async registerPickPositionDelegate(callback: (position: XyPosition | null) => void) {
    await this.ensureScreenshotExists();
    this.view = 'screenshot';
    this.isFullscreenMode = true;
    this.pickPositionDelegate = (position: XyPosition | null) => {
      this.isFullscreenMode = false;
      callback(position);
    };
  }

  async registerPickBoundingBoxDelegate(callback: (boundingBox: BoundingBox | null) => void) {
    await this.ensureScreenshotExists();
    this.view = 'screenshot';
    this.isFullscreenMode = true;
    this.pickBoundingBoxDelegate = (boundingBox: BoundingBox | null) => {
      this.isFullscreenMode = false;
      callback(boundingBox);
    };
  }

  private getScrapeRequest(): GqlScrapeRequestInput {

    let prerender: InputMaybe<GqlScrapePrerenderInput>;

    if (this.formGroup.value.renderEngine === 'chrome') {
      prerender = {
        waitUntil: GqlPuppeteerWaitUntil.Load,
        viewport: {
          isMobile: false,
          height: this.formGroup.value.screenResolution.height,
          width: this.formGroup.value.screenResolution.width,
        },
      }
    }

    return {
      page: {
        url: this.formGroup.value.url,
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
      this.errorMessage = scrapeResponse.errorMessage;
    } else {
      this.responseChanged.emit(scrapeResponse);
      this.totalTime =
        (
          (scrapeResponse.debug.metrics.queue + scrapeResponse.debug.metrics.render) /
          1000
        ).toFixed(2) + 's';
      this.view = 'markup';
      console.log('response.debug.contentType', scrapeResponse.debug.contentType);
      const url = this.formGroup.value.url;
      this.embedMarkup = {
        mimeType: scrapeResponse.debug.contentType,
        data: scrapeResponse.debug.html,
        url,
        viewport: scrapeResponse.debug.viewport,
      };
      if (scrapeResponse.debug.screenshot) {
        this.view = 'screenshot';
        this.embedScreenshot = {
          mimeType: 'image/png',
          data: scrapeResponse.debug.screenshot,
          url,
          viewport: scrapeResponse.debug.viewport,
        };
      }
    }
  }

  screenResolutionLabelProvider(sr: ScreenResolution): string {
    return `${sr.name} ${sr.width}x${sr.height}`;
  }

  private async ensureScreenshotExists() {
    console.log('ensureScreenshotExists')
    if (!this.embedScreenshot && this.formGroup.value.renderEngine !== 'chrome') {
      this.formGroup.controls.renderEngine.setValue('chrome')
    }
  }

  isPickAnyModeActive(): boolean {
    return isDefined(this.pickPositionDelegate) || isDefined(this.pickElementDelegate) || isDefined(this.pickBoundingBoxDelegate)
  }

  getPickModeLabel(): string {
    if (isDefined(this.pickPositionDelegate)) {
      return 'Pick a position';
    }
    if (isDefined(this.pickElementDelegate)) {
      return 'Pick an element';
    }
    if (isDefined(this.pickBoundingBoxDelegate)) {
      return 'Draw a bounding box';
    }
    throw new Error('not supported');
  }

  resetPickMode() {
    this.pickPositionDelegate = null;
    this.pickElementDelegate = null;
    this.pickBoundingBoxDelegate = null;
    this.isFullscreenMode = false;
    this.changeRef.detectChanges();
  }
}
