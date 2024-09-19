import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { debounce, interval, merge } from 'rxjs';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Location } from '@angular/common';
import { BoundingBox, XyPosition } from '../../components/embedded-image/embedded-image.component';
import { GqlRecordField, GqlScrapeActionInput, GqlScrapeEmit, GqlSourceInput } from '../../../generated/graphql';
import { AlertController, ItemReorderEventDetail } from '@ionic/angular';
import { RepositoryService } from '../../services/repository.service';
import { fixUrl, isValidUrl } from '../../app.module';
import { ActivatedRoute, Router } from '@angular/router';
import { SessionService } from '../../services/session.service';
import { ServerConfigService } from '../../services/server-config.service';
import { createEmailFormControl } from '../../form-controls';
import { Title } from '@angular/platform-browser';
import { DEFAULT_FETCH_CRON } from '../feed-builder/feed-builder.page';
import { ScrapeService } from '../../services/scrape.service';
import { BrowserAction, InteractiveWebsiteController } from '../../modals/interactive-website-modal/interactive-website-controller';

type Email = string;

type Screen = 'area' | 'page' | 'element';

@Component({
  selector: 'app-tracker-edit-page',
  templateUrl: './tracker-edit.page.html',
  styleUrls: ['./tracker-edit.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TrackerEditPage extends InteractiveWebsiteController implements OnInit, OnDestroy {
  // additionalWait = new FormControl<number>(0, [
  //   Validators.required,
  //   Validators.min(0),
  //   Validators.max(10),
  // ]);
  form = new FormGroup(
    {
      url: new FormControl<string>('', [
        Validators.required,
        Validators.minLength(10),
        Validators.pattern(new RegExp('https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)'))
      ]),
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
      compareType: new FormControl<GqlRecordField>(GqlRecordField.Pixel, [
        Validators.required,
      ]),
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
  protected readonly GqlRecordField = GqlRecordField;
  // errorMessage: null;
  showErrors: boolean;
  isThrottled: boolean;
  screenArea: Screen = 'area';
  screenPage: Screen = 'page';
  screenElement: Screen = 'element';
  protected isLoading: boolean = false;
  protected hasUrl: boolean = false;
  source: GqlSourceInput;

  constructor(
    private readonly router: Router,
    private readonly activatedRoute: ActivatedRoute,
    private readonly location: Location,
    private readonly sessionService: SessionService,
    private readonly titleService: Title,
    private readonly alertCtrl: AlertController,
    private readonly repositoryService: RepositoryService,
    public readonly changeRef: ChangeDetectorRef,
    public readonly scrapeService: ScrapeService,
    public readonly serverConfig: ServerConfigService,
  ) {
    super()
  }

  ngOnInit() {
    this.titleService.setTitle('Tracker Builder');
    this.isThrottled = !this.serverConfig.isSelfHosted();
    this.subscriptions.push(
      this.actionsFg.valueChanges
        .pipe(debounce(() => interval(800)))
        .subscribe(() => {
          if (this.actionsFg.valid) {
            this.sourceBuilder.overwriteFlow(
              this.getActionsRequestFragment(),
            );
            this.sourceBuilder.events.actionsChanges.next();
          }
        }),

      this.form.controls.url.valueChanges
        .subscribe((url) => {
          if (this.form.controls.url.valid) {
            return this.initialize(url);
          }
        }),
      // this.activatedRoute.queryParams.subscribe((queryParams) => {
      //   if (queryParams.url && queryParams.url != this.form.value.url) {
      //     this.form.controls.url.setValue(queryParams.url);
      //   }
      // }),
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

  private async initialize(url: string) {
    console.log('initialize scrape '+ url);

    if (url.trim().length < 10) {
      return;
    }

    this.hasUrl = true;

    // await this.router.navigate(['.'], {
    //   queryParams: {
    //     url,
    //   },
    //   relativeTo: this.activatedRoute,
    //   skipLocationChange: true,
    // });

    this.source = {
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
          // ...this.getActionsRequestFragment(),
          this.getEmit(),
        ],
      },
    };

    this.initializeController();

    this.changeRef.detectChanges();
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
    throw new Error('not implemented')
    // const sub = await this.repositoryService.createRepositories({
    //   repositories: [
    //     {
    //       sources: [
    //         {
    //           title: `From ${this.form.value.url}`,
    //           tags: [],
    //           flow: {
    //             sequence: [
    //               {
    //                 fetch: {
    //                   get: {
    //                     url: {
    //                       literal: this.form.value.url,
    //                     },
    //                     additionalWaitSec: this.additionalWait.value,
    //                   },
    //                 },
    //               },
    //               ...this.getActionsRequestFragment(),
    //               this.getEmit(),
    //             ],
    //           },
    //         },
    //       ],
    //       product: environment.product,
    //       additionalSinks: [
    //         {
    //           email: this.form.value.email,
    //         },
    //       ],
    //       title: this.form.value.subject,
    //       description: 'Visual Diff',
    //       withShareKey: false,
    //       refreshCron: this.form.value.fetchFrequency,
    //       retention: {
    //         maxCapacity: 2,
    //       },
    //       plugins: [
    //         {
    //           pluginId: GqlFeedlessPlugins.OrgFeedlessDiffEmailForward,
    //           params: {
    //             [GqlFeedlessPlugins.OrgFeedlessDiffEmailForward]: {
    //               inlineDiffImage: true,
    //               inlineLatestImage: true,
    //               compareBy: {
    //                 field: this.form.value.compareType,
    //               },
    //               nextItemMinIncrement: this.form.value.sinkCondition,
    //             },
    //           },
    //         },
    //       ],
    //     },
    //   ],
    // });
    //
    // if (!this.sessionService.isAuthenticated()) {
    //   await this.showAnonymousSuccessAlert();
    // }
    // await this.router.navigateByUrl(`/subscriptions/${sub[0].id}`);
  }

  pickArea() {
    this.sourceBuilder.events.pickArea.next((boundingBox: BoundingBox) => {
      this.form.controls.areaBoundingBox.patchValue(boundingBox);
      this.changeRef.detectChanges();
    });
  }

  pickPoint(action: FormGroup<BrowserAction>) {
    console.log('pickPoint')
    this.sourceBuilder.events.pickPoint.next((position: XyPosition) => {
      action.controls.clickParams.patchValue(position);
      this.changeRef.detectChanges();
    });
  }

  pickXPath() {
    this.sourceBuilder.events.pickElement.next((xpath: string) => {
      this.form.controls.elementXpath.setValue(xpath);
      this.changeRef.detectChanges();
    });
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

  private async patchUrlInAddressBar() {
    const url = this.router
      .createUrlTree(this.activatedRoute.snapshot.url, {
        queryParams: {  },
        relativeTo: this.activatedRoute,
      })
      .toString();
    this.location.replaceState(url);
  }

  private getEmit(): GqlScrapeActionInput {
    if (this.form.value.screen === 'area') {
      return {
        extract: {
          fragmentName: 'element from bounding box',
          imageBased: {
            boundingBox: this.form.value.areaBoundingBox,
          },
        },
      };
    } else {
      return {
        extract: {
          fragmentName: 'element from xpath',
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

  handleQuery(url: string) {
    if (this.form.value.url !== url) {
      if (isValidUrl(url)) {
        this.form.controls.url.setValue(url);
      } else {
        const fixedUrl = fixUrl(url);
        this.form.controls.url.setValue(fixedUrl);
      }
    }
  }

  // private async showAnonymousSuccessAlert() {
  //   const alert = await this.alertCtrl.create({
  //     header: 'Tracker created',
  //     cssClass: 'success-alert',
  //     message: `You should have received an email, you may continue from there.`,
  //     buttons: ['Ok'],
  //   });
  //
  //   await alert.present();
  // }

  getXPathLabel(formControl: FormControl<string | null>) {
    if (formControl.valid) {
      return formControl.value;
    } else {
      return 'Choose Element';
    }
  }
}
