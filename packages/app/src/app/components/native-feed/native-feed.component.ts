import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnInit,
} from '@angular/core';
import { Article, ArticleService } from '../../services/article.service';
import { FeedService, NativeFeed } from '../../services/feed.service';
import {
  ActionSheetButton,
  ActionSheetController,
  AlertController,
  ModalController,
  ToastController,
} from '@ionic/angular';
import { Pagination } from '../../services/pagination.service';
import {
  GqlArticleType,
  GqlArticleReleaseStatus,
  GqlContentCategoryTag,
  GqlContentTypeTag,
} from '../../../generated/graphql';
import {
  FilterQuery,
  Filters,
} from '../filter-toolbar/filter-toolbar.component';
import { SubscribeModalComponent } from '../../modals/subscribe-modal/subscribe-modal.component';
import { FilteredList } from '../filtered-list';
import { FetchPolicy } from '@apollo/client/core';
import { FormControl } from '@angular/forms';

@Component({
  selector: 'app-native-feed',
  templateUrl: './native-feed.component.html',
  styleUrls: ['./native-feed.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NativeFeedComponent
  extends FilteredList<Article, FilterQuery>
  implements OnInit
{
  @Input()
  id: string;

  loading: boolean;
  feed: NativeFeed;
  filters: Filters = {
    tag: {
      name: 'tag',
      control: new FormControl<GqlContentCategoryTag[]>([]),
      options: Object.values(GqlContentCategoryTag),
    },
    content: {
      name: 'content',
      control: new FormControl<GqlContentTypeTag[]>(
        Object.values(GqlContentTypeTag)
      ),
      options: Object.values(GqlContentTypeTag),
    },
    status: {
      name: 'status',
      control: new FormControl<GqlArticleReleaseStatus[]>([
        GqlArticleReleaseStatus.Released,
      ]),
      options: Object.values(GqlArticleReleaseStatus),
    },
    type: {
      name: 'type',
      control: new FormControl<GqlArticleType[]>([GqlArticleType.Feed]),
      options: Object.values(GqlArticleType),
    },
  };

  constructor(
    private readonly articleService: ArticleService,
    private readonly modalCtrl: ModalController,
    private readonly toastCtrl: ToastController,
    private readonly alertCtrl: AlertController,
    private readonly feedService: FeedService,
    private readonly changeRef: ChangeDetectorRef,
    readonly actionSheetCtrl: ActionSheetController
  ) {
    super('feed', actionSheetCtrl);
  }

  async ngOnInit() {
    await this.fetchFeed();
  }

  getBulkActionButtons(): ActionSheetButton<any>[] {
    return [];
  }

  onDidChange() {
    this.changeRef.detectChanges();
  }

  fetch(filterData: FilterQuery): Promise<[Article[], Pagination]> {
    return this.articleService
      .findAllByStreamId(
        this.feed.streamId,
        this.currentPage,
        filterData.query,
        [GqlArticleType.Feed],
        [GqlArticleReleaseStatus.Released, GqlArticleReleaseStatus.Unreleased]
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

    const result = await actionSheet.onDidDismiss();
  }

  async openSubscribeModal() {
    const modal = await this.modalCtrl.create({
      component: SubscribeModalComponent,
    });
    await modal.present();
  }

  async handleFeedAction(event: any) {
    switch (event.detail.value) {
      case 'edit':
        await this.showFeedEdit();
        break;
      case 'delete':
        await this.feedService.deleteNativeFeed(this.id);
        break;
    }
  }

  private async showFeedEdit() {
    const alert = await this.alertCtrl.create({
      header: 'Edit Feed',
      buttons: [
        {
          text: 'Cancel',
          role: 'cancel',
          handler: () => {},
        },
        {
          text: 'Save',
          role: 'confirm',
          handler: () => {},
        },
      ],
      inputs: [
        {
          name: 'title',
          placeholder: 'title',
          attributes: {
            required: true,
          },
          value: this.feed.title,
        },
        {
          name: 'feedUrl',
          type: 'url',
          placeholder: 'Feed URL',
          attributes: {
            required: true,
          },
          min: 1,
          max: 200,
          value: this.feed.feedUrl,
        },
        {
          name: 'description',
          placeholder: 'Description',
          type: 'textarea',
        },
      ],
    });

    await alert.present();
    const { data } = await alert.onDidDismiss();

    if (data) {
      const toast = await this.toastCtrl.create({
        message: 'Updated',
        duration: 3000,
        color: 'success',
      });

      await toast.present();
      await this.fetchFeed('network-only');
    } else {
      const toast = await this.toastCtrl.create({
        message: 'Canceled',
        duration: 3000,
      });

      await toast.present();
    }
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
      fetchPolicy
    );
    this.loading = false;
    this.changeRef.detectChanges();
  }
}
