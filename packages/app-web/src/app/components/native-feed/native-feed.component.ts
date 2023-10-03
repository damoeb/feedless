import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { ArticleService } from '../../services/article.service';
import { FeedService } from '../../services/feed.service';
import {
  ActionSheetButton,
  ActionSheetController,
  AlertController,
  ModalController,
  ToastController,
} from '@ionic/angular';
import {
  FilterData,
  Filters,
} from '../filter-toolbar/filter-toolbar.component';
import { FilteredList } from '../filtered-list';
import { FetchPolicy } from '@apollo/client/core';
import { ServerSettingsService } from '../../services/server-settings.service';
import { toOrderBy } from '../../pages/feeds/feeds.page';
import {
  articleFilters,
  ArticlesFilterValues,
} from '../articles/articles.component';
import {
  GqlNativeFeedStatus,
  GqlPuppeteerWaitUntil,
} from '../../../generated/graphql';
import { ProfileService } from '../../services/profile.service';
import { Authentication, AuthService } from 'src/app/services/auth.service';
import {
  WizardComponent,
  WizardComponentProps,
  WizardContext,
  WizardExitRole,
  WizardStepId,
} from '../wizard/wizard/wizard.component';
import { Subscription } from 'rxjs';
import { Article, NativeFeed, Pagination } from '../../graphql/types';
import { Router } from '@angular/router';

@Component({
  selector: 'app-native-feed',
  templateUrl: './native-feed.component.html',
  styleUrls: ['./native-feed.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NativeFeedComponent
  extends FilteredList<Article, FilterData<ArticlesFilterValues>>
  implements OnInit, OnDestroy
{
  @Input()
  id: string;

  loading: boolean;
  feed: NativeFeed;
  filters: Filters<ArticlesFilterValues>;
  authorization: Authentication;
  feedUrl: string;

  private subscriptions: Subscription[] = [];

  constructor(
    private readonly articleService: ArticleService,
    private readonly profileService: ProfileService,
    private readonly modalCtrl: ModalController,
    private readonly toastCtrl: ToastController,
    private readonly alertCtrl: AlertController,
    private readonly feedService: FeedService,
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly serverSettingsService: ServerSettingsService,
    private readonly changeRef: ChangeDetectorRef,
    readonly actionSheetCtrl: ActionSheetController,
  ) {
    super(actionSheetCtrl);
  }

  async ngOnInit() {
    await this.fetchFeed();
    this.filters = articleFilters(
      this.feed.ownerId === this.profileService.getUserId(),
    );
    this.changeRef.detectChanges();

    this.subscriptions.push(
      this.authService
        .authorizationChange()
        .subscribe(async (authorization) => {
          this.authorization = authorization;
          this.changeRef.detectChanges();
        }),
    );
    this.feedUrl = `${this.serverSettingsService.apiUrl}/stream/feed/${this.feed.id}`;
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  getBulkActionButtons(): ActionSheetButton<any>[] {
    return [];
  }

  onDidChange() {
    this.changeRef.detectChanges();
  }

  fetch(
    filterData: FilterData<ArticlesFilterValues>,
    page: number,
    fetchPolicy: FetchPolicy,
  ): Promise<[Article[], Pagination]> {
    return this.articleService
      .findAllByStreamId(
        {
          cursor: {
            page,
          },
          where: {
            stream: {
              id: {
                equals: this.feed.streamId,
              },
            },
            status: {
              oneOf: filterData.filters.status,
            },
            type: {
              oneOf: filterData.filters.type,
            },
          },
          orderBy: toOrderBy(filterData.sortBy),
        },
        fetchPolicy,
      )
      .then((response) => [response.articles, response.pagination]);
  }

  async showBulkActions() {
    const actionSheet = await this.actionSheetCtrl.create({
      header: `Actions for ${this.checkedEntities.length} Articles`,
      buttons: [
        {
          text: 'Forward',
          role: 'destructive',
          handler: () => {
            // todo mag
          },
        },
        {
          text: 'Trigger Plugin',
          role: 'destructive',
          handler: () => {
            // todo mag
          },
        },
      ],
    });

    await actionSheet.present();
    await actionSheet.onDidDismiss();
  }

  // async openSubscribeModal() {
  //   const feedUrl = `${this.serverSettingsService.apiUrl}/stream/feed/${this.feed.id}`;
  //   const componentProps: SubscribeModalComponentProps = {
  //     jsonFeedUrl: `${feedUrl}/json`,
  //     atomFeedUrl: `${feedUrl}/atom`,
  //     filter: this.filterData,
  //   };
  //   const modal = await this.modalCtrl.create({
  //     component: SubscribeModalComponent,
  //     componentProps,
  //   });
  //   await modal.present();
  // }

  async handleDelete() {
    await this.feedService.deleteNativeFeed(this.id);
    const toast = await this.toastCtrl.create({
      message: 'Deleted',
      duration: 3000,
      color: 'success',
    });

    await toast.present();
    await this.router.navigateByUrl('/');
  }

  hasProblems(status: GqlNativeFeedStatus): boolean {
    return [
      GqlNativeFeedStatus.NotFound,
      GqlNativeFeedStatus.NeverFetched,
      GqlNativeFeedStatus.Defective,
    ].includes(status);
  }

  async fixFeedUrl() {
    const componentProps: WizardComponentProps = {
      initialContext: {
        exitAfterStep: [
          WizardStepId.refineGenericFeed,
          WizardStepId.refineNativeFeed,
        ],
        modalTitle: 'Fix Feed',
        fetchOptions: {
          websiteUrl: this.getPrimaryUrl(),
          prerender: false,
          prerenderWaitUntil: GqlPuppeteerWaitUntil.Load,
        },
      },
    };
    const modal = await this.modalCtrl.create({
      component: WizardComponent,
      componentProps,
      showBackdrop: true,
      backdropDismiss: false,
    });
    await modal.present();
    const { role, data } = await modal.onDidDismiss<WizardContext>();

    if (role === WizardExitRole.persistFeed) {
      console.log(data);
      await this.feedService.updateNativeFeed({
        where: {
          id: this.feed.id,
        },
        data: {
          feedUrl: {
            set: data.feedUrl,
          },
          harvestRateFixed: {
            set: true,
          },
        },
      });
    }
  }

  async handleEdit() {
    await this.fixFeedUrl();
  }

  private async fetchFeed(fetchPolicy: FetchPolicy = 'cache-first') {
    this.loading = true;
    this.changeRef.detectChanges();
    this.feed = await this.feedService.getNativeFeed(
      {
        where: {
          id: this.id,
        },
      },
      fetchPolicy,
    );
    this.loading = false;
    this.changeRef.detectChanges();
  }

  private getPrimaryUrl(): string {
    const feedHost = new URL(this.feed.feedUrl).hostname;
    if (this.feed.websiteUrl) {
      const websiteHost = new URL(this.feed.websiteUrl).hostname;
      if (feedHost !== websiteHost) {
        return this.feed.websiteUrl;
      } else {
        return this.feed.feedUrl;
      }
    } else {
      return this.feed.feedUrl;
    }
  }
}
