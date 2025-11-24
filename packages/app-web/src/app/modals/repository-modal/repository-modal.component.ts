import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnInit,
} from '@angular/core';
import {
  IonAccordion,
  IonAccordionGroup,
  IonButton,
  IonButtons,
  IonCheckbox,
  IonCol,
  IonContent,
  IonHeader,
  IonIcon,
  IonInput,
  IonItem,
  IonItemDivider,
  IonLabel,
  IonList,
  IonNote,
  IonRadio,
  IonRadioGroup,
  IonRow,
  IonSelect,
  IonSelectOption,
  IonText,
  IonTextarea,
  IonTitle,
  IonToolbar,
  ModalController,
  ToastController,
} from '@ionic/angular/standalone';
import {
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { RepositoryService } from '../../services/repository.service';
import {
  GqlConditionalTagInput,
  GqlFeatureName,
  GqlFeedlessPlugins,
  GqlItemFilterParamsInput,
  GqlPluginExecutionInput,
  GqlRecordDateField,
  GqlSourceInput,
  GqlStringFilterOperator,
  GqlVisibility,
} from '../../../generated/graphql';
import { Router, RouterLink } from '@angular/router';
import { environment } from '../../../environments/environment';
import { dateFormat, SessionService } from '../../services/session.service';
import { RepositoryFull } from '../../graphql/types';
import { ServerConfigService } from '../../services/server-config.service';
import { ArrayElement, isDefined, Nullable, TypedFormGroup } from '../../types';
import { omit, without } from 'lodash-es';
import { addIcons } from 'ionicons';
import { closeOutline, ellipsisHorizontalOutline, flaskOutline } from 'ionicons/icons';
import { DEFAULT_FETCH_CRON } from '../../defaults';
import { FeatureService } from '../../services/feature.service';
import {
  FilterField,
  FilterItemsAccordionComponent,
  FilterOperator,
} from '../../components/filter-items-accordion/filter-items-accordion.component';
import { ModalService } from '../../services/modal.service';
import { FetchRateAccordionComponent } from '../../components/fetch-rate-accordion/fetch-rate-accordion.component';
import { RemoveIfProdDirective } from '../../directives/remove-if-prod/remove-if-prod.directive';
import { JsonPipe, KeyValuePipe } from '@angular/common';
import { BubbleComponent } from '../../components/bubble/bubble.component';
import { FlowModalComponent } from '../flow-modal/flow-modal.component';

export interface RepositoryModalComponentProps {
  repository: RepositoryFull;
  openAccordions?: RepositoryModalAccordion[];
}

interface TagConditionData {
  tag: string;
  field: FilterField;
  operator: FilterOperator;
  value: string;
}

type ConditionalTagParams = ArrayElement<
  ArrayElement<RepositoryFull['plugins']>['params']['org_feedless_conditional_tag']
>;

export type FulltextTransformer = 'none' | 'summary' | 'readability';

export type RepositoryModalAccordion = 'privacy' | 'storage' | 'notifications' | 'plugins';

type RepositoryFormGroupDef = {
  title: FormControl<string>;
  description: FormControl<string>;
  maxCapacity: FormControl<number>;
  maxAgeDays: FormControl<number>;
  ageReferenceField: FormControl<GqlRecordDateField>;
  fetchFrequency: FormControl<string>;
  applyFulltextPlugin: FormControl<boolean>;
  fulltextTransformer: FormControl<FulltextTransformer>;
  isPublic: FormControl<boolean>;
  applyPrivacyPlugin: FormControl<boolean>;
  enabledPushNotifications: FormControl<boolean>;
  applyConditionalTagsPlugin: FormControl<boolean>;
};

@Component({
  selector: 'app-repository-modal',
  templateUrl: './repository-modal.component.html',
  styleUrls: ['./repository-modal.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [
    IonHeader,
    IonToolbar,
    IonButtons,
    IonButton,
    IonIcon,
    IonTitle,
    IonLabel,
    IonContent,
    IonList,
    IonRow,
    IonCol,
    IonInput,
    FormsModule,
    ReactiveFormsModule,
    IonTextarea,
    FetchRateAccordionComponent,
    IonAccordionGroup,
    IonAccordion,
    IonItem,
    IonNote,
    IonCheckbox,
    IonRadioGroup,
    IonRadio,
    RemoveIfProdDirective,
    IonSelect,
    IonSelectOption,
    RouterLink,
    FilterItemsAccordionComponent,
    JsonPipe,
    KeyValuePipe,
    BubbleComponent,
    IonText,
    IonItemDivider,
  ],
})
export class RepositoryModalComponent implements RepositoryModalComponentProps, OnInit {
  private readonly modalCtrl = inject(ModalController);
  private readonly toastCtrl = inject(ToastController);
  private readonly sessionService = inject(SessionService);
  private readonly featureService = inject(FeatureService);
  readonly serverConfig = inject(ServerConfigService);
  private readonly router = inject(Router);
  private readonly changeRef = inject(ChangeDetectorRef);
  private readonly repositoryService = inject(RepositoryService);
  private readonly modalService = inject(ModalService);

  formFg: FormGroup<RepositoryFormGroupDef>;
  conditionalTags: FormGroup<TypedFormGroup<TagConditionData>>[] = [];

  summaryTransformer: FulltextTransformer = 'summary';
  noTransformer: FulltextTransformer = 'none';
  readabilityTransformer: FulltextTransformer = 'readability';

  repository: RepositoryFull;
  openAccordions: RepositoryModalAccordion[] = [];

  loading = false;
  errorMessage: string;
  isLoggedIn: boolean;
  showExpertOptions: boolean = false;

  protected readonly dateFormat = dateFormat;
  protected readonly GqlRecordDateField = GqlRecordDateField;
  accordionPrivacy: RepositoryModalAccordion = 'privacy';
  accordionNotifications: RepositoryModalAccordion = 'notifications';
  accordionCustomPlugins: RepositoryModalAccordion = 'plugins';
  accordionStorage: RepositoryModalAccordion = 'storage';
  protected readonly GqlStringFilterOperator = GqlStringFilterOperator;
  protected FilterFieldLink: FilterField = 'link';
  protected FilterFieldTitle: FilterField = 'title';
  protected FilterFieldContent: FilterField = 'content';

  private filterParams: GqlItemFilterParamsInput[] = [];
  protected repositoryMaxItemsUpperLimit: Nullable<number> = null;

  constructor() {
    addIcons({ closeOutline, ellipsisHorizontalOutline, flaskOutline });
  }

  closeModal() {
    return this.modalCtrl.dismiss();
  }

  addConditionalTag(data: ConditionalTagParams = null) {
    if (this.conditionalTags.some((filter) => filter.invalid)) {
      return;
    }

    const filter = new FormGroup({
      tag: new FormControl<string>('', [Validators.required]),
      field: new FormControl<FilterField>('title', [Validators.required]),
      operator: new FormControl<FilterOperator>(GqlStringFilterOperator.Contains, [
        Validators.required,
      ]),
      value: new FormControl<string>('', [Validators.required, Validators.minLength(1)]),
    });

    // todo implement
    // if (data) {
    //   const type = Object.keys(data).find(
    //     (field) => field != '__typename' && !!data[field],
    //   );
    //   const field = Object.keys(data[type]).find(
    //     (field) => field != '__typename' && !!data[type][field],
    //   );
    //   filter.patchValue({
    //     tag: type as any,
    //     field: field as any,
    //     value: data[type][field].value,
    //     operator: data[type][field].operator,
    //   });
    // }

    this.conditionalTags.push(filter);
    filter.statusChanges.subscribe((status) => {
      if (status === 'VALID') {
        // this.filterChanges.next();
      }
    });
  }

  removeConditionalTag(index: number) {
    this.conditionalTags = without(this.conditionalTags, this.conditionalTags[index]);
    // this.filterChanges.next();
  }

  private getConditionalTagsParams(): GqlConditionalTagInput[] {
    return this.conditionalTags
      .filter((filterFg) => filterFg.valid)
      .map((filterFg) => filterFg.value)
      .map<GqlConditionalTagInput>((data) => ({
        tag: data.tag,
        filter: {
          [data.field]: {
            value: data.value,
            operator: data.operator,
          },
        },
      }));
  }

  async createOrUpdateFeed() {
    if (this.formFg.invalid) {
      console.warn('form is invalid', this.formFg.errors);
      return;
    }

    this.loading = true;
    this.changeRef.detectChanges();

    try {
      const plugins: GqlPluginExecutionInput[] = [];

      const hasFilters = this.filterParams.length > 0;
      const applyFiltersLast = false;

      const appendFilterPlugin = () => {
        plugins.push({
          pluginId: GqlFeedlessPlugins.OrgFeedlessFilter,
          params: {
            org_feedless_filter: this.filterParams,
            org_feedless_conditional_tag: this.getConditionalTagsParams(),
          },
        });
      };

      if (!applyFiltersLast && hasFilters) {
        appendFilterPlugin();
      }
      if (this.formFg.value.applyFulltextPlugin) {
        plugins.push({
          pluginId: GqlFeedlessPlugins.OrgFeedlessFulltext,
          params: {
            org_feedless_fulltext: {
              readability: this.formFg.value.fulltextTransformer == 'readability',
              summary: this.formFg.value.fulltextTransformer == 'summary',
              inheritParams: true,
            },
          },
        });
      }
      if (this.formFg.value.applyPrivacyPlugin) {
        plugins.push({
          pluginId: GqlFeedlessPlugins.OrgFeedlessPrivacy,
          params: {},
        });
      }
      if (applyFiltersLast && hasFilters) {
        appendFilterPlugin();
      }
      if (this.isUpdate()) {
        const {
          title,
          isPublic,
          description,
          fetchFrequency,
          maxAgeDays,
          maxCapacity,
          ageReferenceField,
        } = this.formFg.value;
        await this.repositoryService.updateRepository({
          where: {
            id: this.repository.id,
          },
          data: {
            plugins,
            visibility: {
              set: isPublic ? GqlVisibility.IsPublic : GqlVisibility.IsPrivate,
            },
            title: {
              set: title,
            },
            description: {
              set: description,
            },
            refreshCron: {
              set: fetchFrequency,
            },
            retention: {
              maxCapacity: {
                set: maxCapacity || null,
              },
              maxAgeDays: {
                set: maxAgeDays || null,
              },
              ageReferenceField: {
                set: ageReferenceField,
              },
            },
          },
        });
        const toast = await this.toastCtrl.create({
          message: 'Saved',
          duration: 3000,
          color: 'success',
        });

        await toast.present();
      } else {
        const repositories = await this.repositoryService.createRepositories([
          {
            product: environment.product,
            sources: this.repository.sources.map<GqlSourceInput>(
              (source) => omit(source, 'recordCount') as GqlSourceInput
            ),
            title: this.formFg.value.title,
            refreshCron: this.formFg.value.fetchFrequency,
            withShareKey: true,
            description: this.formFg.value.description,
            visibility: this.formFg.value.isPublic
              ? GqlVisibility.IsPublic
              : GqlVisibility.IsPrivate,
            plugins,
          },
        ]);

        const firstRepository = repositories[0];
        await this.modalCtrl.dismiss();
        await this.router.navigateByUrl(`/feeds/${firstRepository.id}`);
      }
    } catch (e: any) {
      this.errorMessage = e?.message;
    } finally {
      this.loading = false;
    }
    this.changeRef.detectChanges();
  }

  async ngOnInit() {
    this.formFg = new FormGroup({
      title: new FormControl<string>('', [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(255),
      ]),
      description: new FormControl<string>('', [Validators.maxLength(500)]),
      maxCapacity: new FormControl<number>(null, [Validators.min(2)]),
      maxAgeDays: new FormControl<number>(null, [Validators.min(1)]),
      ageReferenceField: new FormControl<GqlRecordDateField>(GqlRecordDateField.PublishedAt),
      fetchFrequency: new FormControl<string>(DEFAULT_FETCH_CRON, {
        nonNullable: true,
        validators: Validators.pattern('([^ ]+ ){5}[^ ]+'),
      }),
      applyFulltextPlugin: new FormControl<boolean>(false),
      fulltextTransformer: new FormControl<FulltextTransformer>('none'),
      isPublic: new FormControl<boolean>(false),
      applyPrivacyPlugin: new FormControl<boolean>(false),
      enabledPushNotifications: new FormControl<boolean>(false),
      applyConditionalTagsPlugin: new FormControl<boolean>(false),
    });
    this.isLoggedIn = this.sessionService.isAuthenticated();

    const canPublicRepository = this.featureService.getFeatureValueBool(
      GqlFeatureName.PublicRepository
    );
    if (!canPublicRepository) {
      this.formFg.controls.isPublic.disable();
      this.formFg.controls.isPublic.setErrors({
        disabled: 'Feature disabled for you',
      });
    }

    const maxItemsLowerLimit = this.featureService.getFeatureValueInt(
      GqlFeatureName.RepositoryCapacityLowerLimitInt
    );
    if (maxItemsLowerLimit) {
      this.formFg.controls.maxCapacity.addValidators([Validators.min(maxItemsLowerLimit)]);
    }
    const maxItemsUpperLimit = this.featureService.getFeatureValueInt(
      GqlFeatureName.RepositoryCapacityUpperLimitInt
    );
    if (maxItemsUpperLimit) {
      this.repositoryMaxItemsUpperLimit = maxItemsUpperLimit;
      // this.formFg.controls.maxCapacity.addValidators([
      //   Validators.max(maxItemsUpperLimit),
      // ]);
    }

    const maxDaysLowerLimit = this.featureService.getFeatureValueInt(
      GqlFeatureName.RepositoryRetentionMaxDaysLowerLimitInt
    );
    if (maxDaysLowerLimit) {
      this.formFg.controls.maxAgeDays.addValidators([Validators.min(maxDaysLowerLimit)]);
    }

    const retention = this.repository.retention;
    const fulltextPlugin = this.repository.plugins.find(
      (p) => p.pluginId === GqlFeedlessPlugins.OrgFeedlessFulltext
    );
    this.formFg.patchValue({
      title: this.repository.title.substring(0, 255),
      isPublic: this.repository.visibility == GqlVisibility.IsPublic,
      description: this.repository.description,
      fetchFrequency: this.repository.refreshCron || DEFAULT_FETCH_CRON,
      applyFulltextPlugin: !!fulltextPlugin,
      fulltextTransformer: fulltextPlugin?.params?.org_feedless_fulltext?.summary
        ? 'summary'
        : fulltextPlugin?.params?.org_feedless_fulltext?.readability
          ? 'readability'
          : 'none',
      applyPrivacyPlugin: this.repository.plugins.some(
        (p) => p.pluginId === GqlFeedlessPlugins.OrgFeedlessPrivacy
      ),
      enabledPushNotifications: this.repository.pushNotificationsEnabled,
      maxAgeDays: retention?.maxAgeDays || null,
      maxCapacity: retention?.maxCapacity || null,
    });
    this.formFg.markAllAsTouched();
  }

  isUpdate() {
    return isDefined(this.repository.id);
  }

  getFilterPlugin() {
    return this.repository.plugins.find((p) => p.pluginId === GqlFeedlessPlugins.OrgFeedlessFilter)
      ?.params?.org_feedless_filter;
  }

  handleFilterChange(filterParams: GqlItemFilterParamsInput[]) {
    this.filterParams = filterParams ?? [];
  }

  getPathname(): string {
    return encodeURI(location.href);
  }

  openFlowModal() {
    return this.modalService.openFlowModal(FlowModalComponent, {});
  }
}
