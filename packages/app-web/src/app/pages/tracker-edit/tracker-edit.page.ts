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
  GqlFeedlessPlugins,
  GqlRecordField,
  GqlScrapeActionInput,
  GqlScrapeEmit,
  GqlScrapeExtractInput,
  GqlSourceInput,
  GqlVertical,
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
  IonInput,
  IonItem,
  IonLabel,
  IonList,
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
import { createEmailFormControl } from '../../form-controls';

type Email = string;

type PageFragmentType = 'area' | 'page' | 'element';
type BaselineControl = 'manual' | 'latest';

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
    IonInput,
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
      baseline: new FormControl<BaselineControl>('latest', [
        Validators.required,
      ]),
      email: createEmailFormControl<Email>(''),
      // compareBy: new FormControl<PageFragmentType>('page', [
      //   Validators.required,
      // ]),
      output: new FormControl<PageFragmentType>('page', [Validators.required]),
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
      outputTypes: new FormControl<GqlRecordField[]>(
        [GqlRecordField.Pixel, GqlRecordField.Markup],
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
  // errorMessage: null;
  showErrors: boolean;
  baselineManual: BaselineControl = 'manual';
  baselineLatest: BaselineControl = 'latest';
  pageFragmentArea: PageFragmentType = 'area';
  pageFragmentPage: PageFragmentType = 'page';
  pageFragmentElement: PageFragmentType = 'element';
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

    this.formGroup.controls.email.disable();
    this.formGroup.controls.baseline.disable();

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

      // this.activatedRoute.params.subscribe(params => {
      //   if (params.url) {
      //     this.formGroup.controls.url.setValue(params.url);
      //   }
      // }),
      this.activatedRoute.queryParams.subscribe((queryParams) => {
        if (queryParams.url && queryParams.url != this.formGroup.value.url) {
          this.formGroup.controls.url.setValue(queryParams.url);
        }
      }),
      // this.formGroup.controls.compareBy.valueChanges.subscribe(
      this.formGroup.controls.output.valueChanges.subscribe(
        (pageFragmentType) => {
          console.log('compareBy', pageFragmentType);
          this.formGroup.controls.elementXpath.disable();
          this.formGroup.controls.areaBoundingBox.disable();

          if (pageFragmentType === 'area') {
            this.formGroup.controls.areaBoundingBox.enable();
          }
          if (pageFragmentType === 'element') {
            this.formGroup.controls.elementXpath.enable();
          }

          this.changeRef.detectChanges();
        },
      ),
      this.formGroup.controls.compareType.valueChanges.subscribe(
        (compareType) => {
          if (compareType === 'pixel') {
            // this.formGroup.controls.compareBy.enable();
            this.formGroup.controls.output.enable();
            this.formGroup.controls.elementXpath.disable();
          } else {
            // this.formGroup.controls.compareBy.disable();
            this.formGroup.controls.output.disable();
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

  async createTracker() {
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
      console.log('assign xpath', xpath);
      this.formGroup.controls.elementXpath.setValue(xpath);
      console.log('valid', this.formGroup.controls.elementXpath.valid);
      console.log('disabled', this.formGroup.controls.elementXpath.disabled);
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
      title: `Track ${url}`,
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
          // this.getEmit(),
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
    const sources: GqlSourceInput[] = [
      {
        title: `Track ${this.formGroup.value.url}`,
        tags: [],
        flow: {
          sequence: [...this.getActionsRequestFragment(), this.getEmit()],
        },
      },
    ];

    const sub = await this.repositoryService.createRepositories([
      {
        sources,
        product: GqlVertical.VisualDiff,
        // additionalSinks: [
        //   {
        //     email: this.form.value.email,
        //   },
        // ],
        title: this.formGroup.value.subject,
        description: 'Visual Diff',
        // withShareKey: false,
        refreshCron: this.formGroup.value.fetchFrequency,
        retention: {
          maxCapacity: 2,
        },
        plugins: [
          {
            pluginId: GqlFeedlessPlugins.OrgFeedlessDiffRecords,
            params: {
              [GqlFeedlessPlugins.OrgFeedlessDiffRecords]: {
                inlineDiffImage: true,
                inlineLatestImage: true,
                compareBy: {
                  field: this.formGroup.value.compareType,
                },
                nextItemMinIncrement: this.formGroup.value.sinkCondition,
              },
            },
          },
        ],
      },
    ]);

    // if (!this.sessionService.isAuthenticated()) {
    //   await this.showAnonymousSuccessAlert();
    // }
    await this.router.navigateByUrl(`/subscriptions/${sub[0].id}`);
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
    if (this.formGroup.value.output === 'area') {
      const extract: GqlScrapeExtractInput = {
        fragmentName: 'output',
        imageBased: {
          boundingBox: this.formGroup.value.areaBoundingBox,
        },
      };
      return {
        extract,
      };
    } else {
      return {
        extract: {
          fragmentName: 'output',
          selectorBased: {
            fragmentName: 'element from xpath',
            uniqueBy: this.getUniqueBy(this.formGroup.value.compareType),
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

  private getUniqueBy(compareType: GqlRecordField): GqlScrapeEmit {
    switch (compareType) {
      case GqlRecordField.Markup:
        return GqlScrapeEmit.Html;
      case GqlRecordField.Text:
        return GqlScrapeEmit.Text;
      case GqlRecordField.Pixel:
        return GqlScrapeEmit.Pixel;
    }
    throw new Error(`Cannot map compareType ${compareType} to GqlScrapeEmit`);
  }
}
