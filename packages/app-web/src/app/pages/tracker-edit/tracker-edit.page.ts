import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { debounce, interval, merge, Subscription } from 'rxjs';
import { FormArray, FormControl, FormGroup, Validators } from '@angular/forms';
import {
  BoundingBox,
  XyPosition,
} from '../../components/embedded-image/embedded-image.component';
import {
  GqlFeedlessPlugins,
  GqlScrapeActionInput,
  GqlScrapeEmit,
  GqlSourceInput,
  GqlWebDocumentField,
  GqlXyPosition,
} from '../../../generated/graphql';
import { isEqual, isNull, isUndefined } from 'lodash-es';
import { AlertController, ItemReorderEventDetail } from '@ionic/angular';
import { RepositoryService } from '../../services/repository.service';
import { fixUrl, isValidUrl } from '../../app.module';
import { ActivatedRoute, Router } from '@angular/router';
import { SessionService } from '../../services/session.service';
import { environment } from '../../../environments/environment';
import { ServerConfigService } from '../../services/server-config.service';
import { createEmailFormControl } from '../../form-controls';
import { ScrapeController } from '../../components/interactive-website/scrape-controller';
import { Title } from '@angular/platform-browser';
import { DEFAULT_FETCH_CRON } from '../feed-builder/feed-builder.page';

type Email = string;

type Screen = 'area' | 'page' | 'element';
type BrowserActionType = keyof GqlScrapeActionInput;

interface BrowserAction {
  type: FormControl<BrowserActionType>;
  clickParams: FormControl<GqlXyPosition>;
}

@Component({
  selector: 'app-tracker-edit-page',
  templateUrl: './tracker-edit.page.html',
  styleUrls: ['./tracker-edit.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TrackerEditPage implements OnInit, OnDestroy {
  // pickPositionDelegate: (position: GqlXyPosition | null) => void;
  // pickXPathDelegate: (xpath: string | null) => void;
  // pickBoundingBoxDelegate: (boundingBox: BoundingBox | null) => void;
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
      fetchFrequency: new FormControl<string>(DEFAULT_FETCH_CRON, [
        Validators.required,
      ]),
      subject: new FormControl<string>('', [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(255),
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
  protected readonly GqlWebDocumentField = GqlWebDocumentField;
  private subscriptions: Subscription[] = [];
  // errorMessage: null;
  private scrapeRequest: GqlSourceInput;
  showErrors: boolean;
  isThrottled: boolean;
  screenArea: Screen = 'area';
  screenPage: Screen = 'page';
  screenElement: Screen = 'element';
  protected scrapeController: ScrapeController;
  protected isLoading: boolean = false;

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly router: Router,
    private readonly activatedRoute: ActivatedRoute,
    private readonly sessionService: SessionService,
    private readonly titleService: Title,
    private readonly serverConfig: ServerConfigService,
    private readonly alertCtrl: AlertController,
    private readonly repositoryService: RepositoryService,
  ) {}

  ngOnInit() {
    this.titleService.setTitle('Tracker Builder');
    this.isThrottled = !this.serverConfig.isSelfHosted();
    this.subscriptions.push(
      this.actions.valueChanges
        .pipe(debounce(() => interval(800)))
        .subscribe(() => {
          if (this.actions.valid) {
            this.scrapeController.scrapeRequest.flow.sequence =
              this.getActionsRequestFragment();
            this.scrapeController.actionsChanges.emit();
          }
        }),

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

  async scrape() {
    let url = this.form.value.url;
    console.log('scrape', url);
    if (!isValidUrl(url)) {
      url = fixUrl(url);
      this.form.controls.url.setValue(fixUrl(url), { emitEvent: false });
    }

    if (this.form.controls.url.invalid) {
      return;
    }

    await this.router.navigate(['.'], {
      queryParams: {
        url,
      },
      relativeTo: this.activatedRoute,
      skipLocationChange: true,
    });

    const newScrapeRequest: GqlSourceInput = {
      title: `From ${url}`,
      flow: {
        sequence: [
          {
            fetch: {
              get: {
                url: {
                  literal: url,
                },
                forcePrerender: true,
              },
            },
          },
          ...this.getActionsRequestFragment(),
          this.getEmit(),
        ],
      },
    };

    if (isEqual(newScrapeRequest, this.scrapeRequest)) {
      console.log('scrapeRequest is unchanged');
    } else {
      this.scrapeRequest = newScrapeRequest;

      // const scrapeResponse = await this.scrapeService.scrape(
      //   this.scrapeRequest,
      // );

      this.scrapeController = new ScrapeController(this.scrapeRequest);

      // const fetchAction = scrapeResponse.outputs.find((o) => o.response.fetch)
      //   .response.fetch;
      // const extractAction = scrapeResponse.outputs.find(
      //   (o) => o.response.extract,
      // ).response.extract;
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
              title: `From ${this.form.value.url}`,
              tags: [],
              flow: {
                sequence: [
                  {
                    fetch: {
                      get: {
                        url: {
                          literal: this.form.value.url,
                        },
                        additionalWaitSec: this.additionalWait.value,
                      },
                    },
                  },
                  ...this.getActionsRequestFragment(),
                  this.getEmit(),
                ],
              },
            },
          ],
          product: environment.product,
          additionalSinks: [
            {
              email: this.form.value.email,
            },
          ],
          sinkOptions: {
            title: this.form.value.subject,
            description: 'Visual Diff',
            withShareKey: false,
            refreshCron: this.form.value.fetchFrequency,
            retention: {
              maxCapacity: 2,
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
    this.scrapeController.pickArea.emit((boundingBox: BoundingBox) => {
      this.form.controls.areaBoundingBox.patchValue(boundingBox);
      this.changeRef.detectChanges();
    });
  }

  pickPosition(action: FormGroup<BrowserAction>) {
    this.scrapeController.pickPoint.emit((position: XyPosition) => {
      action.controls.clickParams.patchValue(position);
      this.changeRef.detectChanges();
    });
  }

  pickXPath() {
    this.scrapeController.pickElement.emit((xpath: string) => {
      this.form.controls.elementXpath.setValue(xpath);
      this.changeRef.detectChanges();
    });
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

  protected isDefined(v: any | undefined): boolean {
    return !isNull(v) && !isUndefined(v);
  }

  private getEmit(): GqlScrapeActionInput {
    if (this.form.value.screen === 'area') {
      return {
        extract: {
          fragmentName: 'element',
          imageBased: {
            boundingBox: this.form.value.areaBoundingBox,
          },
        },
      };
    } else {
      return {
        extract: {
          fragmentName: 'element',
          selectorBased: {
            fragmentName: '',
            xpath: {
              value: this.form.controls.elementXpath.enabled
                ? this.form.value.elementXpath
                : '/',
            },
            emit: [GqlScrapeEmit.Pixel, GqlScrapeEmit.Html],
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
