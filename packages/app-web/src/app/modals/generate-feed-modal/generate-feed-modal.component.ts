import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnInit,
} from '@angular/core';
import { AlertController, ModalController } from '@ionic/angular';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { TypedFormGroup } from '../../components/scrape-source/scrape-source.component';
import { SourceSubscriptionService } from '../../services/source-subscription.service';
import {
  GqlFeedlessPlugins,
  GqlScrapePageInput,
  GqlScrapeRequest,
  GqlVisibility,
} from '../../../generated/graphql';
import { NativeOrGenericFeed } from '../transform-website-to-feed-modal/transform-website-to-feed-modal.component';
import { Router } from '@angular/router';

export interface GenerateFeedModalComponentProps {
  scrapeRequest: GqlScrapeRequest;
  feed: NativeOrGenericFeed;
}

type FilterOperator = 'contains' | 'startsWith' | 'matches' | 'endsWith';
type FilterField = 'link' | 'title' | 'content';
type FilterType = 'include' | 'exclude';

interface FilterData {
  type: FilterType;
  field: FilterField;
  operator: FilterOperator;
  value: string;
}

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
    fetchFrequency: new FormControl<string>('0 0 0 * * *', {
      nonNullable: true,
      validators: Validators.pattern('([^ ]+ ){5}[^ ]+'),
    }),
  });
  filters: FormGroup<TypedFormGroup<FilterData>>[] = [];

  @Input({ required: true })
  feed: NativeOrGenericFeed;

  @Input({ required: true })
  scrapeRequest: GqlScrapeRequest;
  loading = false;
  errorMessage: string;

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly alertCtrl: AlertController,
    private readonly router: Router,
    private readonly changeRef: ChangeDetectorRef,
    private readonly sourceSubscriptionService: SourceSubscriptionService,
  ) {}

  closeModal() {
    return this.modalCtrl.dismiss();
  }

  addFilter() {
    if (this.filters.some((filter) => filter.invalid)) {
      return;
    }

    this.filters.push(
      new FormGroup({
        type: new FormControl<FilterType>('exclude', [Validators.required]),
        field: new FormControl<FilterField>('title', [Validators.required]),
        operator: new FormControl<FilterOperator>('startsWith', [
          Validators.required,
        ]),
        value: new FormControl<string>('', [
          Validators.required,
          Validators.minLength(3),
        ]),
      }),
    );
  }

  removeFilter(index: number) {
    this.filters.slice(index, index + 1);
  }

  async createFeed() {
    if (this.formFg.invalid) {
      return;
    }

    this.loading = true;
    this.changeRef.detectChanges();

    const pageUrl = (): GqlScrapePageInput => {
      if (this.feed.nativeFeed) {
        return {
          url: this.feed.nativeFeed.feedUrl,
        };
      } else {
        return this.scrapeRequest.page;
      }
    };
    try {
      const page: GqlScrapePageInput = pageUrl();

      const subscriptions =
        await this.sourceSubscriptionService.createSubscriptions({
          subscriptions: [
            {
              sources: [
                {
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
                                genericFeed: this.feed.genericFeed?.selectors,
                              },
                            },
                          ],
                        },
                      },
                    },
                  ],
                },
              ],
              sourceOptions: {
                refreshCron: this.formFg.value.fetchFrequency,
              },
              sinkOptions: {
                title: this.formFg.value.title,
                description: this.formFg.value.description,
                visibility: GqlVisibility.IsPrivate,
              },
            },
          ],
        });

      const sub = subscriptions[0];

      await this.modalCtrl.dismiss();
      await this.router.navigateByUrl(`/feeds/${sub.id}`);
    } catch (e) {
      this.errorMessage = e.message;
    } finally {
      this.loading = false;
    }
    this.changeRef.detectChanges();
  }

  async ngOnInit(): Promise<void> {
    this.formFg.patchValue(this.getFeedTitle());
  }

  private getFeedTitle() {
    if (this.feed.nativeFeed) {
      return {
        title: this.feed.nativeFeed.title,
        description: `Source: ${this.feed.nativeFeed.feedUrl}`,
      };
    } else {
      return {
        title: `Feed from ${this.scrapeRequest.page.url}`,
        description: `Source: ${this.scrapeRequest.page.url}`,
      };
    }
  }
}
