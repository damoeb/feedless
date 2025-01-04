import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { debounce, interval } from 'rxjs';
import {
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Location, NgClass } from '@angular/common';
import {
  BoundingBox,
  XyPosition,
} from '../../components/embedded-image/embedded-image.component';
import {
  GqlRecordField,
  GqlScrapeActionInput,
  GqlScrapeEmit,
  GqlSourceInput,
} from '../../../generated/graphql';
import {
  AlertController,
  IonAccordion,
  IonAccordionGroup,
  IonButton,
  IonCol,
  IonContent,
  IonGrid,
  IonIcon,
  IonItem,
  IonLabel,
  IonList,
  IonRadio,
  IonRadioGroup,
  IonReorder,
  IonReorderGroup,
  IonRow,
  IonSelect,
  IonSelectOption,
  ItemReorderEventDetail,
} from '@ionic/angular/standalone';
import { RepositoryService } from '../../services/repository.service';
import { fixUrl, isValidUrl } from '../../app.module';
import { ActivatedRoute, Router } from '@angular/router';
import { SessionService } from '../../services/session.service';
import { ServerConfigService } from '../../services/server-config.service';
import { createEmailFormControl } from '../../form-controls';
import { ScrapeService } from '../../services/scrape.service';
import {
  BrowserAction,
  InteractiveWebsiteController,
} from '../../modals/interactive-website-modal/interactive-website-controller';
import { AppConfigService } from '../../services/app-config.service';
import { addIcons } from 'ionicons';
import { trashOutline } from 'ionicons/icons';
import { SearchbarComponent } from '../../elements/searchbar/searchbar.component';
import { InteractiveWebsiteComponent } from '../../components/interactive-website/interactive-website.component';
import { DEFAULT_FETCH_CRON } from '../../defaults';
import { Nullable } from '../../types';

type Email = string;

type Screen = 'area' | 'page' | 'element';

@Component({
  selector: 'app-tracker-edit-page',
  templateUrl: './tracker-edit.page.html',
  styleUrls: ['./tracker-edit.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IonContent,
    NgClass,
    FormsModule,
    IonGrid,
    IonRow,
    IonCol,
    SearchbarComponent,
    InteractiveWebsiteComponent,
    IonAccordionGroup,
    IonAccordion,
    IonItem,
    IonLabel,
    IonList,
    IonReorderGroup,
    IonSelect,
    ReactiveFormsModule,
    IonSelectOption,
    IonButton,
    IonIcon,
    IonReorder,
    IonRadioGroup,
    IonRadio,
  ],
  standalone: true,
})
export class TrackerEditPage
  extends InteractiveWebsiteController
  implements OnInit, OnDestroy
{
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly location = inject(Location);
  private readonly sessionService = inject(SessionService);
  private readonly appConfig = inject(AppConfigService);
  private readonly alertCtrl = inject(AlertController);
  private readonly repositoryService = inject(RepositoryService);
  readonly changeRef = inject(ChangeDetectorRef);
  readonly scrapeService = inject(ScrapeService);
  readonly serverConfig = inject(ServerConfigService);

  // additionalWait = new FormControl<number>(0, [
  //   Validators.required,
  //   Validators.min(0),
  //   Validators.max(10),
  // ]);
  formGroup = new FormGroup(
    {
      url: new FormControl<string>('', [
        Validators.required,
        Validators.minLength(10),
        Validators.pattern(
          new RegExp(
            'https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)',
          ),
        ),
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
  // errorMessage: null;
  showErrors: boolean;
  screenArea: Screen = 'area';
  screenPage: Screen = 'page';
  screenElement: Screen = 'element';
  source: GqlSourceInput;
  protected readonly GqlRecordField = GqlRecordField;
  protected isLoading: boolean = false;
  protected hasUrl: boolean = false;

  constructor() {
    super();
    addIcons({ trashOutline });
  }

  ngOnInit() {
    this.appConfig.setPageTitle('Tracker Builder');
    this.subscriptions.push(
      this.actionsFg.valueChanges
        .pipe(debounce(() => interval(800)))
        .subscribe(() => {
          if (this.actionsFg.valid) {
            this.sourceBuilder.overwriteFlow(this.getActionsRequestFragment());
            this.sourceBuilder.events.actionsChanges.next();
          }
        }),

      this.formGroup.controls.url.valueChanges.subscribe((url) => {
        if (this.formGroup.controls.url.valid) {
          return this.initialize(url);
        }
      }),
      // this.activatedRoute.queryParams.subscribe((queryParams) => {
      //   if (queryParams.url && queryParams.url != this.form.value.url) {
      //     this.form.controls.url.setValue(queryParams.url);
      //   }
      // }),
      this.formGroup.controls.screen.valueChanges.subscribe((screen) => {
        if (screen === 'area') {
          this.formGroup.controls.areaBoundingBox.enable();
        } else {
          this.formGroup.controls.areaBoundingBox.disable();
        }
        this.changeRef.detectChanges();
      }),
      this.formGroup.controls.compareType.valueChanges.subscribe(
        (compareType) => {
          if (compareType === 'pixel') {
            this.formGroup.controls.screen.enable();
            this.formGroup.controls.elementXpath.disable();
          } else {
            this.formGroup.controls.screen.disable();
            this.formGroup.controls.elementXpath.enable();
          }
          this.changeRef.detectChanges();
        },
      ),
    );

    this.changeRef.detectChanges();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
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

  pickArea() {
    this.sourceBuilder.events.pickArea.next((boundingBox: BoundingBox) => {
      this.formGroup.controls.areaBoundingBox.patchValue(boundingBox);
      this.changeRef.detectChanges();
    });
  }

  pickPoint(action: FormGroup<BrowserAction>) {
    console.log('pickPoint');
    this.sourceBuilder.events.pickPoint.next((position: XyPosition) => {
      action.controls.clickParams.patchValue(position);
      this.changeRef.detectChanges();
    });
  }

  pickXPath() {
    this.sourceBuilder.events.pickElement.next((xpath: string) => {
      this.formGroup.controls.elementXpath.setValue(xpath);
      this.changeRef.detectChanges();
    });
  }

  getPositionLabel(action: FormGroup<BrowserAction>): Nullable<string> {
    const clickParams = action.value.clickParams;
    if (clickParams) {
      return `(${clickParams.x}, ${clickParams.y})`;
    }
  }

  getBoundingBoxLabel(
    action: FormControl<BoundingBox | null>,
  ): Nullable<string> {
    const boundingBox = action.value;
    if (boundingBox) {
      return `(${boundingBox.x}, ${boundingBox.y}; ${boundingBox.w}, ${boundingBox.h})`;
    }
  }

  handleQuery(url: string) {
    if (this.formGroup.value.url !== url) {
      if (isValidUrl(url)) {
        this.formGroup.controls.url.setValue(url);
      } else {
        const fixedUrl = fixUrl(url);
        this.formGroup.controls.url.setValue(fixedUrl);
      }
    }
  }

  getXPathLabel(formControl: FormControl<string | null>): Nullable<string> {
    if (formControl.valid) {
      return formControl.value;
    }
  }

  private async initialize(url: string) {
    console.log('initialize scrape ' + url);

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

  private async createSubscription() {
    if (this.formGroup.invalid) {
      return;
    }
    throw new Error('not implemented');
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

  private async patchUrlInAddressBar() {
    const url = this.router
      .createUrlTree(this.activatedRoute.snapshot.url, {
        queryParams: {},
        relativeTo: this.activatedRoute,
      })
      .toString();
    this.location.replaceState(url);
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

  private getEmit(): GqlScrapeActionInput {
    if (this.formGroup.value.screen === 'area') {
      return {
        extract: {
          fragmentName: 'element from bounding box',
          imageBased: {
            boundingBox: this.formGroup.value.areaBoundingBox,
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
              value: this.formGroup.controls.elementXpath.enabled
                ? this.formGroup.value.elementXpath
                : '/',
            },
            emit: [GqlScrapeEmit.Pixel, GqlScrapeEmit.Html],
          },
        },
      };
    }
  }
}
