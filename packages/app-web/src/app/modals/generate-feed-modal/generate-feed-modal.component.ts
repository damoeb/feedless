import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnInit,
  ViewChild,
} from '@angular/core';
import { AlertController, ModalController } from '@ionic/angular';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { TypedFormGroup } from '../../components/scrape-source/scrape-source.component';
import { SourceSubscriptionService } from '../../services/source-subscription.service';
import {
  GqlCompositeFieldFilterParamsInput,
  GqlCompositeFilterParamsInput,
  GqlFeedlessPlugins,
  GqlPluginExecutionInput,
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
import { FeedService } from '../../services/feed.service';
import { SourceSubscription } from '../../graphql/types';
import { ServerSettingsService } from '../../services/server-settings.service';
import { isDefined } from '../scrape-source-modal/scrape-builder';
import { ArrayElement } from '../../types';
import { RemoteFeedPreviewComponent } from '../../components/remote-feed-preview/remote-feed-preview.component';

export interface GenerateFeedModalComponentProps {
  subscription: SourceSubscription;
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
  ArrayElement<SourceSubscription['plugins']>['params']['org_feedless_filter']
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
    applyPrivacyPlugin: new FormControl<boolean>(false),
  });
  filters: FormGroup<TypedFormGroup<FilterData>>[] = [];

  @Input({ required: true })
  subscription: SourceSubscription;

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

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly alertCtrl: AlertController,
    private readonly profileService: SessionService,
    private readonly feedService: FeedService,
    readonly serverSettings: ServerSettingsService,
    private readonly router: Router,
    private readonly changeRef: ChangeDetectorRef,
    private readonly sourceSubscriptionService: SourceSubscriptionService,
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
      if (this.filters.length > 0) {
        plugins.push({
          pluginId: GqlFeedlessPlugins.OrgFeedlessFilter,
          params: {
            org_feedless_filter: this.getFilterParams(),
          },
        });
      }

      if (this.isUpdate()) {
        const { title, description, fetchFrequency, maxAgeDays, maxItems } =
          this.formFg.value;
        await this.sourceSubscriptionService.updateSubscription({
          where: {
            id: this.subscription.id,
          },
          data: {
            sinkOptions: {
              plugins,
              title: {
                set: title,
              },
              description: {
                set: description,
              },
              scheduleExpression: {
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
      } else {
        const subscriptions =
          await this.sourceSubscriptionService.createSubscriptions({
            subscriptions: [
              {
                product: environment.product,
                sources: this.subscription.sources as GqlScrapeRequestInput[],
                sourceOptions: {
                  refreshCron: this.formFg.value.fetchFrequency,
                },
                sinkOptions: {
                  title: this.formFg.value.title,
                  description: this.formFg.value.description,
                  visibility: GqlVisibility.IsPrivate,
                  plugins,
                },
              },
            ],
          });

        const sub = subscriptions[0];
        await this.modalCtrl.dismiss();
        await this.router.navigateByUrl(`/feeds/${sub.id}`);
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
    const retention = this.subscription.retention;
    this.formFg.patchValue({
      title: this.subscription.title,
      description: this.subscription.description,
      fetchFrequency: this.subscription.scheduleExpression || '0 0 0 * * *',
      applyFulltextPlugin: this.subscription.plugins.some(
        (p) => p.pluginId === GqlFeedlessPlugins.OrgFeedlessFulltext,
      ),
      applyPrivacyPlugin: this.subscription.plugins.some(
        (p) => p.pluginId === GqlFeedlessPlugins.OrgFeedlessPrivacy,
      ),
      maxAgeDays: retention?.maxAgeDays,
      maxItems: retention?.maxItems,
    });
    const filterPlugin = this.subscription.plugins.find(
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
      this.subscription.sources as GqlScrapeRequest[],
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
    return isDefined(this.subscription.id);
  }
}
