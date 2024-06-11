import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit, ViewChild } from '@angular/core';
import { ModalController, ToastController } from '@ionic/angular';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { RepositoryService } from '../../services/repository.service';
import {
  GqlCompositeFieldFilterParamsInput,
  GqlCompositeFilterParamsInput,
  GqlConditionalTagInput,
  GqlFeatureName,
  GqlFeedlessPlugins,
  GqlPluginExecutionInput,
  GqlProfileName,
  GqlScrapePage,
  GqlScrapeRequest,
  GqlScrapeRequestInput,
  GqlStringFilterOperator,
  GqlVisibility
} from '../../../generated/graphql';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import { dateFormat, SessionService } from '../../services/session.service';
import { debounce, interval, ReplaySubject } from 'rxjs';
import { without } from 'lodash-es';
import { Repository } from '../../graphql/types';
import { ServerConfigService } from '../../services/server-config.service';
import { ArrayElement, isDefined, TypedFormGroup } from '../../types';
import { RemoteFeedPreviewComponent } from '../../components/remote-feed-preview/remote-feed-preview.component';
import { NativeOrGenericFeed } from '../../components/feed-builder/feed-builder.component';

export interface GenerateFeedModalComponentProps {
  repository: Repository;
  modalTitle?: string;
  openAccordions?: GenerateFeedAccordion[];
}

type FilterOperator = GqlStringFilterOperator;
type FilterField = keyof GqlCompositeFieldFilterParamsInput;
type FilterType = keyof GqlCompositeFilterParamsInput;

interface GeneralFilterData {
  type: FilterType;
  field: FilterField;
  operator: FilterOperator;
  value: string;
}

interface TagConditionData {
  tag: string;
  field: FilterField;
  operator: FilterOperator;
  value: string;
}

export function getScrapeRequest(
  feed: NativeOrGenericFeed,
  scrapeRequest: GqlScrapeRequest,
): GqlScrapeRequest {
  const pageUrl = (): GqlScrapePage => {
    if (feed.nativeFeed) {
      return {
        url: feed.nativeFeed.feedUrl,
      };
    } else {
      return scrapeRequest.page;
    }
  };

  const page = pageUrl();

  return {
    id: null,
    page,
    tags: scrapeRequest.tags,
    emit: [
      {
        selectorBased: {
          xpath: {
            value: '/',
          },
          expose: {
            transformers: [
              {
                pluginId: GqlFeedlessPlugins.OrgFeedlessFeed,
                params: {
                  org_feedless_feed: {
                    generic: feed.genericFeed?.selectors,
                  },
                },
              },
            ],
          },
        },
      },
    ],
  };
}

type GeneralFilterParams = ArrayElement<
  ArrayElement<Repository['plugins']>['params']['org_feedless_filter']
>;

type ConditionalTagParams = ArrayElement<
  ArrayElement<Repository['plugins']>['params']['org_feedless_conditional_tag']
>;

export type GenerateFeedAccordion = 'privacy' | 'storage';

@Component({
  selector: 'app-generate-feed-modal',
  templateUrl: './generate-feed-modal.component.html',
  styleUrls: ['./generate-feed-modal.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GenerateFeedModalComponent
  implements GenerateFeedModalComponentProps, OnInit
{
  formFg = new FormGroup({
    title: new FormControl<string>('', {
      validators: [Validators.required, Validators.minLength(3)],
    }),
    description: new FormControl<string>('', [Validators.maxLength(500)]),
    maxItems: new FormControl<number>(null),
    maxAgeDays: new FormControl<number>(null),
    fetchFrequency: new FormControl<string>('0 0 0 * * *', {
      nonNullable: true,
      validators: Validators.pattern('([^ ]+ ){5}[^ ]+'),
    }),
    applyFulltextPlugin: new FormControl<boolean>(false),
    transformToReadability: new FormControl<boolean>(true),
    applyFiltersLast: new FormControl<boolean>(false),
    isPublic: new FormControl<boolean>(false),
    applyPrivacyPlugin: new FormControl<boolean>(false),
    applyConditionalTagsPlugin: new FormControl<boolean>(false),
  });
  filters: FormGroup<TypedFormGroup<GeneralFilterData>>[] = [];
  conditionalTags: FormGroup<TypedFormGroup<TagConditionData>>[] = [];

  @Input({ required: true })
  repository: Repository;

  @ViewChild('remoteFeedPreviewComponent', { static: true })
  remoteFeedPreview: RemoteFeedPreviewComponent;

  loading = false;
  errorMessage: string;
  isLoggedIn: boolean;
  filterChanges = new ReplaySubject<void>();

  protected readonly dateFormat = dateFormat;
  protected readonly GqlStringFilterOperator = GqlStringFilterOperator;
  protected FilterTypeInclude: FilterType = 'include';
  protected FilterTypeExclude: FilterType = 'exclude';
  protected FilterFieldLink: FilterField = 'link';
  protected FilterFieldTitle: FilterField = 'title';
  protected FilterFieldContent: FilterField = 'content';
  isThrottled: boolean;
  modalTitle = 'Finalize Feed';
  @Input()
  openAccordions: GenerateFeedAccordion[] = [];
  accordionPrivacy: GenerateFeedAccordion = 'privacy';
  accordionStorage: GenerateFeedAccordion = 'storage';

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly toastCtrl: ToastController,
    private readonly sessionService: SessionService,
    readonly serverConfig: ServerConfigService,
    private readonly router: Router,
    private readonly changeRef: ChangeDetectorRef,
    private readonly repositoryService: RepositoryService,
  ) {}

  closeModal() {
    return this.modalCtrl.dismiss();
  }

  addGeneralFilter(data: GeneralFilterParams = null) {
    if (this.filters.some((filter) => filter.invalid)) {
      return;
    }

    const filter = new FormGroup({
      type: new FormControl<FilterType>('exclude', [Validators.required]),
      field: new FormControl<FilterField>('title', [Validators.required]),
      operator: new FormControl<FilterOperator>(
        GqlStringFilterOperator.Contains,
        [Validators.required],
      ),
      value: new FormControl<string>('', [
        Validators.required,
        Validators.minLength(1),
      ]),
    });

    if (data) {
      const type = Object.keys(data).find(
        (field) => field != '__typename' && !!data[field],
      );
      const field = Object.keys(data[type]).find(
        (field) => field != '__typename' && !!data[type][field],
      );
      filter.patchValue({
        type: type as any,
        field: field as any,
        value: data[type][field].value,
        operator: data[type][field].operator,
      });
    }

    this.filters.push(filter);
    filter.statusChanges.subscribe((status) => {
      if (status === 'VALID') {
        this.filterChanges.next();
      }
    });
  }

  addConditionalTag(data: ConditionalTagParams = null) {
    if (this.conditionalTags.some((filter) => filter.invalid)) {
      return;
    }

    const filter = new FormGroup({
      tag: new FormControl<string>('', [Validators.required]),
      field: new FormControl<FilterField>('title', [Validators.required]),
      operator: new FormControl<FilterOperator>(
        GqlStringFilterOperator.Contains,
        [Validators.required],
      ),
      value: new FormControl<string>('', [
        Validators.required,
        Validators.minLength(1),
      ]),
    });

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
        this.filterChanges.next();
      }
    });
  }

  removeFilter(index: number) {
    this.filters = without(this.filters, this.filters[index]);
    this.filterChanges.next();
  }

  removeConditionalTag(index: number) {
    this.conditionalTags = without(
      this.conditionalTags,
      this.conditionalTags[index],
    );
    this.filterChanges.next();
  }

  async createOrUpdateFeed() {
    if (this.formFg.invalid) {
      return;
    }

    this.loading = true;
    this.changeRef.detectChanges();

    try {
      const plugins: GqlPluginExecutionInput[] = [];

      const hasFilters = this.filters.length > 0;
      const applyFiltersLast = this.formFg.value.applyFiltersLast;

      const appendFilterPlugin = () => {
        plugins.push({
          pluginId: GqlFeedlessPlugins.OrgFeedlessFilter,
          params: {
            org_feedless_filter: this.getGeneralFilterParams(),
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
              readability: this.formFg.value.transformToReadability,
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
          maxItems,
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
              maxItems: {
                set: maxItems,
              },
              maxAgeDays: {
                set: maxAgeDays,
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
        const repositories = await this.repositoryService.createRepositories({
          repositories: [
            {
              product: environment.product,
              sources: this.repository.sources as GqlScrapeRequestInput[],
              sourceOptions: {
                refreshCron: this.formFg.value.fetchFrequency,
              },
              sinkOptions: {
                title: this.formFg.value.title,
                description: this.formFg.value.description,
                visibility: this.formFg.value.isPublic
                  ? GqlVisibility.IsPublic
                  : GqlVisibility.IsPrivate,
                plugins,
              },
            },
          ],
        });

        const firstRepository = repositories[0];
        await this.modalCtrl.dismiss();
        await this.router.navigateByUrl(`/feeds/${firstRepository.id}`);
      }
    } catch (e) {
      this.errorMessage = e.message;
    } finally {
      this.loading = false;
    }
    this.changeRef.detectChanges();
  }

  async ngOnInit(): Promise<void> {
    this.isLoggedIn = this.sessionService.isAuthenticated();

    const maxItemsLowerLimit = this.serverConfig.getFeatureValueInt(
      GqlFeatureName.RepositoryCapacityLowerLimitInt,
    );
    if (maxItemsLowerLimit) {
      this.formFg.controls.maxItems.addValidators([
        Validators.min(maxItemsLowerLimit),
      ]);
    }
    const maxItemsUpperLimit = this.serverConfig.getFeatureValueInt(
      GqlFeatureName.RepositoryCapacityUpperLimitInt,
    );
    if (maxItemsUpperLimit) {
      this.formFg.controls.maxItems.addValidators([
        Validators.min(maxItemsUpperLimit),
      ]);
    }

    const maxDaysLowerLimit = this.serverConfig.getFeatureValueInt(
      GqlFeatureName.RepositoryRetentionMaxDaysLowerLimitInt,
    );
    if (maxDaysLowerLimit) {
      this.formFg.controls.maxAgeDays.addValidators([
        Validators.min(maxDaysLowerLimit),
      ]);
    }

    this.isThrottled = !this.serverConfig.hasProfile(GqlProfileName.SelfHosted);

    const retention = this.repository.retention;
    this.formFg.patchValue({
      title: this.repository.title,
      isPublic: this.repository.visibility == GqlVisibility.IsPublic,
      description: this.repository.description,
      fetchFrequency: this.repository.refreshCron || '0 0 0 * * *',
      applyFulltextPlugin: this.repository.plugins.some(
        (p) => p.pluginId === GqlFeedlessPlugins.OrgFeedlessFulltext,
      ),
      applyPrivacyPlugin: this.repository.plugins.some(
        (p) => p.pluginId === GqlFeedlessPlugins.OrgFeedlessPrivacy,
      ),
      maxAgeDays: retention?.maxAgeDays,
      maxItems: retention?.maxItems,
    });
    const filterPlugin = this.repository.plugins.find(
      (p) => p.pluginId === GqlFeedlessPlugins.OrgFeedlessFilter,
    );
    if (filterPlugin) {
      filterPlugin.params.org_feedless_filter.forEach((f) =>
        this.addGeneralFilter(f),
      );
    }

    this.filterChanges
      .pipe(debounce(() => interval(800)))
      .subscribe(async () => {
        console.log('changes');
        await this.loadFeedPreview();
      });
    await this.loadFeedPreview();
  }

  private async loadFeedPreview() {
    await this.remoteFeedPreview.loadFeedPreview(
      this.repository.sources as GqlScrapeRequest[],
      this.getGeneralFilterParams(),
      this.getConditionalTagsParams(),
    );
  }

  private getGeneralFilterParams(): GqlCompositeFilterParamsInput[] {
    return this.filters
      .filter((filterFg) => filterFg.valid)
      .map((filterFg) => filterFg.value)
      .map<GqlCompositeFilterParamsInput>((filter) => ({
        [filter.type]: {
          [filter.field]: {
            value: filter.value,
            operator: filter.operator,
          },
        },
      }));
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

  isUpdate() {
    return isDefined(this.repository.id);
  }
}
