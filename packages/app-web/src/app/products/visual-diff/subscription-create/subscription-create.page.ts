import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { debounce, interval, merge, Subscription } from 'rxjs';
import { FormArray, FormControl, FormGroup, Validators } from '@angular/forms';
import { Embeddable } from '../../../components/embedded-website/embedded-website.component';
import { BoundingBox, XyPosition } from '../../../components/embedded-image/embedded-image.component';
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
} from '../../../../generated/graphql';
import { isNull, isUndefined } from 'lodash-es';
import { ItemReorderEventDetail } from '@ionic/angular';
import { ScrapeService } from '../../../services/scrape.service';
import { ScrapedElement } from '../../../graphql/types';
import { Maybe } from 'graphql/jsutils/Maybe';
import { SourceSubscriptionService } from '../../../services/source-subscription.service';
import { fixUrl, isValidUrl } from '../../../pages/getting-started/getting-started.page';
import { Router } from '@angular/router';

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
  clickParams: FormControl<GqlXyPosition>
}

@Component({
  selector: 'app-visual-diff-create',
  templateUrl: './subscription-create.page.html',
  styleUrls: ['./subscription-create.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SubscriptionCreatePage implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];

  embedScreenshot: Embeddable;
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
    fetchFrequency: new FormControl<string>('0 0 0 * * *', [
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
    ),
    areaBoundingBox: new FormControl<BoundingBox>({disabled: true, value: null}, [Validators.required])
  });

  actions = new FormArray<FormGroup<BrowserAction>>([])

  private scrapeResponse: VisualDiffScrapeResponse;
  busy = false;

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly router: Router,
    private readonly scrapeService: ScrapeService,
    private readonly sourceSubscriptionService: SourceSubscriptionService,
  ) {}

  ngOnInit() {
    this.subscriptions.push(
      merge(
        this.form.controls.url.valueChanges,
        this.actions.valueChanges,
      ).pipe(debounce(() => interval(800)))
        .subscribe(() => {
        if (this.form.controls.url.valid) {
          return this.scrape();
        }
      }),
      this.form.controls.screen.valueChanges.subscribe(screen => {
        if (screen === 'area') {
          this.form.controls.areaBoundingBox.enable()
        } else {
          this.form.controls.areaBoundingBox.disable()
        }
      })
    );

    // this.form.valueChanges.subscribe(() => console.log('changed'));

    // this.form.patchValue({
    //   url: 'https://spiegel.de',
    //   screen: 'page',
    //   compareType: GqlWebDocumentField.Pixel,
    //   fetchFrequency: '0 0 0 * * *',
    //   subject: 'Foo',
    //   sinkCondition: 0.1,
    //   email: 'foo@bar.com'
    // });
    this.changeRef.detectChanges();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
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
          actions: this.getActionsRequestFragment()
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
    if (this.actions.valid) {
      this.actions.push(new FormGroup<BrowserAction>({
        type: new FormControl<BrowserActionType>('click'),
        clickParams: new FormControl<GqlXyPosition>(null, [Validators.required])
      }));
    }
  }

  handleReorderActions(ev: CustomEvent<ItemReorderEventDetail>) {
    console.log('Dragged from index', ev.detail.from, 'to', ev.detail.to);
    ev.detail.complete();
  }

  async startMonitoring() {
    if (this.form.invalid) {
      return
    }
    const sub = await this.sourceSubscriptionService.createSubscriptions({
      subscriptions: [
        {
          sources: [
            {
              page: {
                url: this.form.value.url,
                prerender: {},
                actions: this.getActionsRequestFragment()
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

    await this.router.navigateByUrl(`/s/${sub[0].id}`)
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

  private getActionsRequestFragment(): GqlScrapeActionInput[] {
    return this.getActions()
      .filter(action => action.valid)
      .map(action => {
        return {
          click: {
            position: {
              x: action.value.clickParams.x,
              y: action.value.clickParams.y
            }
          }
        };
      });
  }

  getActions(): FormGroup<BrowserAction>[] {
    const actions: FormGroup<BrowserAction>[] = [];
    for(let i=0; i<this.actions.length; i++) {
      actions.push(this.actions.at(i))
    }

    return actions;
  }

  pickBoundingBox() {
    this.pickBoundingBoxDelegate = (boundingBox: BoundingBox) => {
      this.form.controls.areaBoundingBox.patchValue(boundingBox);
      this.changeRef.detectChanges();
    }
  }

  pickPosition(action: FormGroup<BrowserAction>) {
    this.pickPositionDelegate = (position: XyPosition) => {
      action.controls.clickParams.patchValue(position)
      this.changeRef.detectChanges();
    }
  }

  removeAction(index: number) {
    this.actions.removeAt(index)
  }

  getPositionLabel(action: FormGroup<BrowserAction>) {
    const clickParams = action.value.clickParams;
    if (clickParams) {
      return `(${clickParams.x}, ${clickParams.y})`
    } else {
      return 'Click on Screenshot'
    }
  }
}
