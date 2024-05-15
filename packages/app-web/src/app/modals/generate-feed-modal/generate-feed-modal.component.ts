import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnInit,
  ViewChild,
} from '@angular/core';
import { ModalController, ToastController } from '@ionic/angular';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { RepositoryService } from '../../services/repository.service';
import {
  GqlCompositeFieldFilterParamsInput,
  GqlCompositeFilterParamsInput,
  GqlFeedlessPlugins,
  GqlPluginExecutionInput,
  GqlProfileName,
  GqlScrapePage,
  GqlScrapeRequest,
  GqlScrapeRequestInput,
  GqlStringFilterOperator,
  GqlVisibility,
} from '../../../generated/graphql';
import { NativeOrGenericFeed } from '../transform-website-to-feed-modal/transform-website-to-feed-modal.component';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import { dateFormat, SessionService } from '../../services/session.service';
import { debounce, interval, ReplaySubject } from 'rxjs';
import { without } from 'lodash-es';
import { Repository } from '../../graphql/types';
import { ServerSettingsService } from '../../services/server-settings.service';
import { ArrayElement, isDefined, TypedFormGroup } from '../../types';
import { RemoteFeedPreviewComponent } from '../../components/remote-feed-preview/remote-feed-preview.component';

export interface GenerateFeedModalComponentProps {
  repository: Repository;
  modalTitle?: string;
}

type FilterOperator = GqlStringFilterOperator;
type FilterField = keyof GqlCompositeFieldFilterParamsInput;
type FilterType = keyof GqlCompositeFilterParamsInput;

interface FilterData {
  type: FilterType;
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
                  org_feedless_feed: feed.genericFeed?.selectors,
                },
              },
            ],
          },
        },
      },
    ],
  };
}

type FilterParams = ArrayElement<
  ArrayElement<Repository['plugins']>['params']['org_feedless_filter']
>;

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
    description: new FormControl<string>('', [Validators.maxLength(250)]),
    maxItems: new FormControl<number>(null),
    maxAgeDays: new FormControl<number>(null),
    fetchFrequency: new FormControl<string>('0 0 0 * * *', {
      nonNullable: true,
      validators: Validators.pattern('([^ ]+ ){5}[^ ]+'),
    }),
    applyFulltextPlugin: new FormControl<boolean>(false),
    applyFiltersLast: new FormControl<boolean>(true),
    isPublic: new FormControl<boolean>(false),
    applyPrivacyPlugin: new FormControl<boolean>(false),
  });
  filters: FormGroup<TypedFormGroup<FilterData>>[] = [];

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
  showRetention: boolean;
  protected FilterTypeInclude: FilterType = 'include';
  protected FilterTypeExclude: FilterType = 'exclude';
  protected FilterFieldLink: FilterField = 'link';
  protected FilterFieldTitle: FilterField = 'title';
  protected FilterFieldContent: FilterField = 'content';
  isThrottled: boolean;
  modalTitle = 'Finalize Feed';

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly toastCtrl: ToastController,
    private readonly profileService: SessionService,
    readonly serverSettings: ServerSettingsService,
    private readonly router: Router,
    private readonly changeRef: ChangeDetectorRef,
    private readonly repositoryService: RepositoryService,
  ) {}

  closeModal() {
    return this.modalCtrl.dismiss();
  }

  addFilter(f: FilterParams = null) {
    if (this.filters.some((filter) => filter.invalid)) {
      return;
    }

    const filter = new FormGroup({
      type: new FormControl<FilterType>('exclude', [Validators.required]),
      field: new FormControl<FilterField>('title', [Validators.required]),
      operator: new FormControl<FilterOperator>(
        GqlStringFilterOperator.StartsWidth,
        [Validators.required],
      ),
      value: new FormControl<string>('', [
        Validators.required,
        Validators.minLength(1),
      ]),
    });

    if (f) {
      const type = Object.keys(f).find(
        (field) => field != '__typename' && !!f[field],
      );
      const field = Object.keys(f[type]).find(
        (field) => field != '__typename' && !!f[type][field],
      );
      filter.patchValue({
        type: type as any,
        field: field as any,
        value: f[type][field].value,
        operator: f[type][field].operator,
      });
    }

    this.filters.push(filter);
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
            org_feedless_filter: this.getFilterParams(),
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
              readability: true,
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
            sinkOptions: {
              plugins,
              visibility: {
                set: isPublic
                  ? GqlVisibility.IsPublic
                  : GqlVisibility.IsPrivate,
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
    this.isLoggedIn = this.profileService.isAuthenticated();

    this.isThrottled = !this.serverSettings.hasProfile(
      GqlProfileName.SelfHosted,
    );

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
      filterPlugin.params.org_feedless_filter.forEach((f) => this.addFilter(f));
    }

    this.showRetention =
      isDefined(retention?.maxAgeDays) || isDefined(retention?.maxItems);
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
      this.getFilterParams(),
    );
  }

  private getFilterParams(): GqlCompositeFilterParamsInput[] {
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

  isUpdate() {
    return isDefined(this.repository.id);
  }
}
