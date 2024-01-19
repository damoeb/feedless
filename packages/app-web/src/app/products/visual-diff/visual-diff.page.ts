import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ProfileService } from '../../services/profile.service';
import { Subscription } from 'rxjs';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Embeddable } from '../../components/embedded-website/embedded-website.component';
import { BoundingBox, XyPosition } from '../../components/embedded-image/embedded-image.component';
import {
  GqlFeedlessPlugins,
  GqlScrapeActionInput,
  GqlScrapeDebugResponse,
  GqlScrapeDebugTimes,
  GqlScrapeEmitInput,
  GqlScrapeResponse,
  GqlViewPort,
  GqlWebDocumentField,
  GqlXyPosition
} from '../../../generated/graphql';
import { isNull, isUndefined } from 'lodash-es';
import { ItemReorderEventDetail } from '@ionic/angular';
import { ScrapeService } from '../../services/scrape.service';
import { ScrapedElement } from '../../graphql/types';
import { Maybe } from 'graphql/jsutils/Maybe';
import { SourceSubscriptionService } from '../../services/source-subscription.service';
import { fixUrl, isValidUrl } from '../../pages/getting-started/getting-started.page';
import { Authentication, AuthService } from '../../services/auth.service';

type Email = string;

type VisualDiffScrapeResponse = Pick<
  GqlScrapeResponse,
  'url' | 'failed' | 'errorMessage'
> & {
  debug: Pick<
    GqlScrapeDebugResponse,
    'console' | 'cookies' | 'contentType' | 'statusCode' | 'screenshot' | 'html'
  > & {
    metrics: Pick<GqlScrapeDebugTimes, 'queue' | 'render'>;
    viewport?: Maybe<Pick<GqlViewPort, 'width' | 'height'>>;
  };
  elements: Array<ScrapedElement>;
};

type Screen = 'area' | 'page'
type BrowserActionType = 'click'

interface BrowserAction {
  type: FormControl<BrowserActionType>
  clickParams?: FormControl<GqlXyPosition>
}

@Component({
  selector: 'app-visual-diff',
  templateUrl: './visual-diff.page.html',
  styleUrls: ['./visual-diff.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VisualDiffPage implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];

  isDarkMode: boolean;
  embedScreenshot: Embeddable;
  pickElementDelegate: (xpath: string | null) => void;
  pickPositionDelegate: (position: GqlXyPosition | null) => void;
  pickBoundingBoxDelegate: (boundingBox: BoundingBox | null) => void;

  form = new FormGroup({
    url:new FormControl<string>('', [Validators.required]),
    sinkCondition: new FormControl<number>(0, [
      Validators.required,
      Validators.min(0),
      Validators.max(1),
    ]),
    email: new FormControl<Email>('', [
      Validators.required,
    ]),
    screen: new FormControl<Screen>('page', [
      Validators.required,
    ]),
    fetchFrequency: new FormControl<Email>('0 0 0 * * *', [
      Validators.required,
    ]),
    subject: new FormControl<string>('', [
      Validators.required,
      Validators.minLength(3),
      Validators.maxLength(50)
    ]),
    compareType: new FormControl<GqlWebDocumentField>(
      GqlWebDocumentField.Pixel,
      [Validators.required],
    )
  });

  private scrapeResponse: VisualDiffScrapeResponse;
  authorization: Authentication;
  actions: BrowserAction[] = [];
  busy = false;

  constructor(
    readonly profile: ProfileService,
    private readonly changeRef: ChangeDetectorRef,
    private readonly scrapeService: ScrapeService,
    private readonly authService: AuthService,
    private readonly sourceSubscriptionService: SourceSubscriptionService,
  ) {}

  ngOnInit() {
    this.subscriptions.push(
      this.profile.watchColorScheme().subscribe((isDarkMode) => {
        this.isDarkMode = isDarkMode;
        this.changeRef.detectChanges();
      }),
      this.authService
        .authorizationChange()
        .subscribe(async (authorization) => {
          this.authorization = authorization;
          this.changeRef.detectChanges();
        })
    );

    this.form.patchValue({
      url: 'https://spiegel.de',
      screen: 'page',
      compareType: GqlWebDocumentField.Pixel,
      fetchFrequency: '0 0 0 * * *',
      subject: 'Foo',
      sinkCondition: 0.1,
      email: 'foo@bar.com'
    })
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  handlePickedXpath(xpath: string) {
    if (this.pickElementDelegate) {
      // this.highlightXpath = xpath;
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

  protected isDefined(v: any | undefined): boolean {
    return !isNull(v) && !isUndefined(v);
  }

  handlePickedBoundingBox(boundingBox: BoundingBox | null) {
    if (this.pickBoundingBoxDelegate) {
      this.pickBoundingBoxDelegate(boundingBox);
      this.pickBoundingBoxDelegate = null;
      this.changeRef.detectChanges();
    }
  }

  async scrape() {
    let url = this.form.value.url;
    if (!isValidUrl(url)) {
      url = fixUrl(url);
      this.form.controls.url.setValue(fixUrl(url));
    }
    if (this.busy || this.form.controls.url.invalid) {
      return;
    }
    this.busy = true;
    this.changeRef.detectChanges();

    try {
      const scrapeResponse = await this.scrapeService.scrape({
        page: {
          url,
          prerender: {},
        },
        emit: [],
        debug: {
          screenshot: true,
        },
      });

      this.embedScreenshot = {
        mimeType: 'image/png',
        data: scrapeResponse.debug.screenshot,
        url,
        viewport: scrapeResponse.debug.viewport,
      };
      this.scrapeResponse = scrapeResponse;
    } finally {
      this.busy = false;
    }
    this.changeRef.detectChanges();
  }

  addAction() {
    this.actions.push({
      type: new FormControl<BrowserActionType>('click')
    });
  }

  handleReorderActions(ev: CustomEvent<ItemReorderEventDetail>) {
    console.log('Dragged from index', ev.detail.from, 'to', ev.detail.to);
    ev.detail.complete();
  }

  async startMonitoring() {
    await this.sourceSubscriptionService.createSubscriptions({
      subscriptions: [
        {
          sources: [
            {
              page: {
                url: this.form.value.url,
                prerender: {},
                actions: this.getActions()
              },
              emit: [
                this.getEmit()
              ]
            }
          ],
          additionalSinks: [
            {
              email: this.form.value.email,
            },
          ],
          sourceOptions: {
            refreshCron: this.form.value.fetchFrequency,
          },
          sinkOptions: {
            title: this.form.value.subject,
            description: 'Visual Diff',
            retention: {
              maxItems: 2
            },
            plugins: [
              {
                pluginId: GqlFeedlessPlugins.OrgFeedlessEnforceItemIncrement,
                params: {
                  enforceItemIncrement: {
                    compareBy: this.form.value.compareType,
                    nextItemMinIncrement: this.form.value.sinkCondition,
                  },
                },
              },
            ],
          },
        },
      ],
    });
  }

  protected readonly GqlWebDocumentField = GqlWebDocumentField;

  private getEmit(): GqlScrapeEmitInput {
    if (this.form.value.screen === 'area') {
      return {
        imageBased: {
          boundingBox: null
        }
      };
    } else {
      return {
        selectorBased: {
          xpath: {
            value: '/'
          },
          expose: {
            pixel: this.form.value.compareType === GqlWebDocumentField.Pixel,
          }
        }
      }
    }
  }

  private getActions(): GqlScrapeActionInput[] {
    return [];
  }
}
