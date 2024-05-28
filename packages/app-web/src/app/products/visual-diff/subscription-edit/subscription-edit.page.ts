import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { debounce, interval, merge, Subscription } from 'rxjs';
import { FormArray, FormControl, FormGroup, Validators } from '@angular/forms';
import { Embeddable } from '../../../components/embedded-website/embedded-website.component';
import {
  BoundingBox,
  XyPosition,
} from '../../../components/embedded-image/embedded-image.component';
import {
  GqlFeedlessPlugins,
  GqlScrapeActionInput,
  GqlScrapeDebugResponse,
  GqlScrapeDebugTimes,
  GqlScrapeEmitInput,
  GqlScrapeRequestInput,
  GqlScrapeResponse,
  GqlViewPort,
  GqlWebDocumentField,
  GqlXyPosition,
  Maybe,
} from '../../../../generated/graphql';
import { isEqual, isNull, isUndefined } from 'lodash-es';
import { AlertController, ItemReorderEventDetail } from '@ionic/angular';
import { ScrapeService } from '../../../services/scrape.service';
import { ScrapedElement } from '../../../graphql/types';
import { RepositoryService } from '../../../services/repository.service';
import { fixUrl, isValidUrl } from '../../../app.module';
import { ActivatedRoute, Router } from '@angular/router';
import { SessionService } from '../../../services/session.service';
import { environment } from '../../../../environments/environment';
import { ServerConfigService } from '../../../services/server-config.service';
import { createEmailFormControl } from '../../../form-controls';

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

type Screen = 'area' | 'page' | 'mobile' | 'element';
type BrowserActionType = keyof GqlScrapeActionInput;

interface BrowserAction {
  type: FormControl<BrowserActionType>;
  clickParams: FormControl<GqlXyPosition>;
}

@Component({
  selector: 'app-visual-diff-edit-page',
  templateUrl: './subscription-edit.page.html',
  styleUrls: ['./subscription-edit.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SubscriptionEditPage implements OnInit, OnDestroy {
  embedScreenshot: Embeddable;
  pickPositionDelegate: (position: GqlXyPosition | null) => void;
  pickXPathDelegate: (xpath: string | null) => void;
  pickBoundingBoxDelegate: (boundingBox: BoundingBox | null) => void;
  additionalWait = new FormControl<number>(0, [
    Validators.required,
    Validators.min(0),
    Validators.max(10),
  ]);
  form = new FormGroup(
    {
      url: new FormControl<string>('', [Validators.required]),
      sinkCondition: new FormControl<number>(0, [
        Validators.required,
        Validators.min(0),
        Validators.max(1),
      ]),
      email: createEmailFormControl<Email>(''),
      screen: new FormControl<Screen>('page', [Validators.required]),
      fetchFrequency: new FormControl<string>('0 0 0 * * *', [
        Validators.required,
      ]),
      subject: new FormControl<string>('', [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(50),
      ]),
      compareType: new FormControl<GqlWebDocumentField>(
        GqlWebDocumentField.Pixel,
        [Validators.required],
      ),
      areaBoundingBox: new FormControl<BoundingBox>(
        { disabled: true, value: null },
        [Validators.required],
      ),
      elementXpath: new FormControl<string>({ disabled: true, value: null }, [
        Validators.required,
      ]),
    },
    { updateOn: 'change' },
  );
  actions = new FormArray<FormGroup<BrowserAction>>([]);
  busy = false;
  protected readonly GqlWebDocumentField = GqlWebDocumentField;
  private subscriptions: Subscription[] = [];
  private scrapeResponse: VisualDiffScrapeResponse;
  errorMessage: null;
  private scrapeRequest: GqlScrapeRequestInput;
  showErrors: boolean;
  isThrottled: boolean;
  protected embedWebsite: Embeddable;
  screenArea: Screen = 'area';
  screenPage: Screen = 'page';
  screenMobile: Screen = 'mobile';
  screenElement: Screen = 'element';
  highlightXpath: string;

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly router: Router,
    private readonly activatedRoute: ActivatedRoute,
    private readonly sessionService: SessionService,
    private readonly scrapeService: ScrapeService,
    private readonly serverConfig: ServerConfigService,
    private readonly alertCtrl: AlertController,
    private readonly repositoryService: RepositoryService,
  ) {}

  ngOnInit() {
    this.isThrottled = !this.serverConfig.isSelfHosted();
    this.subscriptions.push(
      merge(
        this.form.controls.url.valueChanges,
        this.actions.valueChanges,
        this.additionalWait.valueChanges,
      )
        .pipe(debounce(() => interval(800)))
        .subscribe(() => {
          if (this.form.controls.url.valid) {
            return this.scrape();
          }
        }),
      this.activatedRoute.queryParams.subscribe((queryParams) => {
        if (queryParams.url && queryParams.url != this.form.value.url) {
          this.form.controls.url.setValue(queryParams.url);
        }
      }),
      this.form.controls.screen.valueChanges.subscribe((screen) => {
        if (screen === 'area') {
          this.form.controls.areaBoundingBox.enable();
        } else {
          this.form.controls.areaBoundingBox.disable();
        }
        this.changeRef.detectChanges();
      }),
      this.form.controls.compareType.valueChanges.subscribe((compareType) => {
        if (compareType === 'pixel') {
          this.form.controls.screen.enable();
          this.form.controls.elementXpath.disable();
        } else {
          this.form.controls.screen.disable();
          this.form.controls.elementXpath.enable();
        }
        this.changeRef.detectChanges();
      }),
    );

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

  handlePickedXPath(xpath: string | null) {
    console.log(xpath);
    if (this.pickXPathDelegate) {
      this.pickXPathDelegate(xpath);
      this.highlightXpath = xpath;
      this.pickXPathDelegate = null;
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

  async scrape() {
    let url = this.form.value.url;
    console.log('scrape', url);
    if (!isValidUrl(url)) {
      url = fixUrl(url);
      this.form.controls.url.setValue(fixUrl(url), { emitEvent: false });
    }

    if (this.busy || this.form.controls.url.invalid) {
      return;
    }

    this.errorMessage = null;
    this.busy = true;
    this.changeRef.detectChanges();

    await this.router.navigate(['.'], {
      queryParams: {
        url,
      },
      relativeTo: this.activatedRoute,
      skipLocationChange: true,
    });

    try {
      const newScrapeRequest = {
        page: {
          url,
          prerender: {
            additionalWaitSec: this.additionalWait.value,
          },
          actions: this.getActionsRequestFragment(),
        },
        emit: [],
        debug: {
          screenshot: true,
          html: true,
        },
      };

      if (isEqual(newScrapeRequest, this.scrapeRequest)) {
        console.log('scrapeRequest is unchanged');
      } else {
        this.scrapeRequest = newScrapeRequest;

        const scrapeResponse = await this.scrapeService.scrape(
          this.scrapeRequest,
        );

        this.embedScreenshot = null;
        this.changeRef.detectChanges();

        this.embedScreenshot = {
          mimeType: 'image/png',
          data: scrapeResponse.debug.screenshot,
          url,
          viewport: scrapeResponse.debug.viewport,
        };

        this.embedWebsite = {
          mimeType: 'text/html',
          data: scrapeResponse.debug.html,
          url: this.form.value.url,
        };
        this.scrapeResponse = scrapeResponse;
      }
    } catch (e) {
      this.errorMessage = e.message;
    } finally {
      this.busy = false;
    }
    this.changeRef.detectChanges();
  }

  addAction() {
    if (this.actions.valid) {
      this.actions.push(
        new FormGroup<BrowserAction>({
          type: new FormControl<BrowserActionType>('click'),
          clickParams: new FormControl<GqlXyPosition>(null, [
            Validators.required,
          ]),
        }),
      );
    }
  }

  handleReorderActions(ev: CustomEvent<ItemReorderEventDetail>) {
    console.log('Dragged from index', ev.detail.from, 'to', ev.detail.to);
    ev.detail.complete();
  }

  async startMonitoring() {
    this.showErrors = true;
    try {
      await this.createSubscription();
    } catch (e) {
      this.errorMessage = e.message;
      this.showErrors = false;
    }
    this.changeRef.detectChanges();
  }
  private async createSubscription() {
    if (this.form.invalid) {
      return;
    }
    const sub = await this.repositoryService.createRepositories({
      repositories: [
        {
          sources: [
            {
              tags: [],
              page: {
                url: this.form.value.url,
                prerender: {
                  additionalWaitSec: this.additionalWait.value,
                },
                actions: this.getActionsRequestFragment(),
              },
              emit: [this.getEmit()],
            },
          ],
          product: environment.product,
          sourceOptions: {
            refreshCron: this.form.value.fetchFrequency,
          },
          additionalSinks: [
            {
              email: this.form.value.email,
            },
          ],
          sinkOptions: {
            title: this.form.value.subject,
            description: 'Visual Diff',
            retention: {
              maxItems: 2,
            },
            plugins: [
              {
                pluginId: GqlFeedlessPlugins.OrgFeedlessDiffEmailForward,
                params: {
                  [GqlFeedlessPlugins.OrgFeedlessDiffEmailForward]: {
                    inlineDiffImage: true,
                    inlineLatestImage: true,
                    compareBy: {
                      field: this.form.value.compareType,
                    },
                    nextItemMinIncrement: this.form.value.sinkCondition,
                  },
                },
              },
            ],
          },
        },
      ],
    });

    if (!this.sessionService.isAuthenticated()) {
      await this.showAnonymousSuccessAlert();
    }
    await this.router.navigateByUrl(`/subscriptions/${sub[0].id}`);
  }

  getActions(): FormGroup<BrowserAction>[] {
    const actions: FormGroup<BrowserAction>[] = [];
    for (let i = 0; i < this.actions.length; i++) {
      actions.push(this.actions.at(i));
    }

    return actions;
  }

  pickBoundingBox() {
    this.pickBoundingBoxDelegate = (boundingBox: BoundingBox) => {
      this.form.controls.areaBoundingBox.patchValue(boundingBox);
      this.changeRef.detectChanges();
    };
  }

  pickPosition(action: FormGroup<BrowserAction>) {
    // action.controls.clickParams.patchValue({ x: 0, y: 0 });
    this.pickPositionDelegate = (position: XyPosition) => {
      action.controls.clickParams.patchValue(position);
      this.changeRef.detectChanges();
    };
  }

  pickXPath() {
    this.pickXPathDelegate = (xpath: string) => {
      this.form.controls.elementXpath.setValue(xpath);
      this.changeRef.detectChanges();
    };
  }

  removeAction(index: number) {
    this.actions.removeAt(index);
  }

  getPositionLabel(action: FormGroup<BrowserAction>) {
    const clickParams = action.value.clickParams;
    if (clickParams) {
      return `(${clickParams.x}, ${clickParams.y})`;
    } else {
      return 'Click on Screenshot';
    }
  }

  getBoundingBoxLabel(action: FormControl<BoundingBox | null>) {
    const boundingBox = action.value;
    if (boundingBox) {
      return `(${boundingBox.x}, ${boundingBox.y}; ${boundingBox.w}, ${boundingBox.h})`;
    } else {
      return 'Draw Area';
    }
  }

  isPickPositionMode() {
    return (
      this.isDefined(this.pickPositionDelegate) ||
      this.isDefined(this.pickXPathDelegate) ||
      this.isDefined(this.pickBoundingBoxDelegate)
    );
  }

  protected isDefined(v: any | undefined): boolean {
    return !isNull(v) && !isUndefined(v);
  }

  private getEmit(): GqlScrapeEmitInput {
    if (this.form.value.screen === 'area') {
      return {
        imageBased: {
          boundingBox: this.form.value.areaBoundingBox,
        },
      };
    } else {
      return {
        selectorBased: {
          xpath: {
            value: this.form.controls.elementXpath.enabled
              ? this.form.value.elementXpath
              : '/',
          },
          expose: {
            pixel: true,
          },
        },
      };
    }
  }

  private getActionsRequestFragment(): GqlScrapeActionInput[] {
    return this.getActions()
      .filter((action) => action.valid)
      .map((action) => {
        return {
          click: {
            position: {
              x: action.value.clickParams.x,
              y: action.value.clickParams.y,
            },
          },
        };
      });
  }

  handleQuery(query: string) {
    this.form.controls.url.setValue(query);
  }

  private async showAnonymousSuccessAlert() {
    const alert = await this.alertCtrl.create({
      header: 'Tracker created',
      cssClass: 'success-alert',
      message: `You should have received an email, you may continue from there.`,
      buttons: ['Ok'],
    });

    await alert.present();
  }

  getXPathLabel(formControl: FormControl<string | null>) {
    if (formControl.valid) {
      return formControl.value;
    } else {
      return 'Choose Element';
    }
  }
}
