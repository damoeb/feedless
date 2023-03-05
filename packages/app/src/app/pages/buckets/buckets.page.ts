import { Component } from '@angular/core';
import { ApolloClient } from '@apollo/client/core';
import {
  ActionSheetButton,
  ActionSheetController,
  ToastController,
} from '@ionic/angular';
import {
  GqlArticleReleaseStatus,
  GqlContentCategoryTag,
  GqlSortOrder,
  GqlVisibility,
} from '../../../generated/graphql';
import { Pagination } from '../../services/pagination.service';
import { BasicBucket, BucketService } from '../../services/bucket.service';
import { FilteredList } from '../../components/filtered-list';
import {
  FilterQuery,
  Filters,
} from '../../components/filter-toolbar/filter-toolbar.component';
import { ProfileService } from 'src/app/services/profile.service';
import { AuthService } from 'src/app/services/auth.service';
import { FormControl } from '@angular/forms';

@Component({
  selector: 'app-buckets-page',
  templateUrl: './buckets.page.html',
  styleUrls: ['./buckets.page.scss'],
})
export class BucketsPage extends FilteredList<BasicBucket, FilterQuery> {
  filters: Filters = {
    tag: {
      name: 'tag',
      control: new FormControl<GqlContentCategoryTag[]>([]),
      options: Object.values(GqlContentCategoryTag),
    },
    status: {
      name: 'status',
      control: new FormControl<GqlArticleReleaseStatus[]>([
        GqlArticleReleaseStatus.Released,
      ]),
      options: Object.values(GqlArticleReleaseStatus),
    },
  };
  constructor(
    private readonly apollo: ApolloClient<any>,
    private readonly bucketService: BucketService,
    private readonly profileService: ProfileService,
    private readonly authService: AuthService,
    private readonly toastCtrl: ToastController,
    readonly actionSheetCtrl: ActionSheetController
  ) {
    super('bucket', actionSheetCtrl);
  }

  getBulkActionButtons(): ActionSheetButton<any>[] {
    return [];
  }

  fetch(filterData: FilterQuery): Promise<[BasicBucket[], Pagination]> {
    return this.bucketService
      .search({
        page: 0,
        where: {
          query: '',
        },
        orderBy: {
          createdAt: GqlSortOrder.Desc,
        },
      })
      .then((response) => [response.buckets, response.pagination]);
  }

  toDate(createdAt: number): Date {
    return new Date(createdAt);
  }

  async showCreateBucketAlert() {
    if (!this.authService.isAuthenticated()) {
      await this.authService.redirectToLogin();
      return;
    }
    const data = await this.bucketService.showBucketAlert('Create Bucket');
    if (data) {
      await this.bucketService.createBucket({
        name: data.title,
        websiteUrl: data.websiteUrl,
        imageUrl: data.imageUrl,
        tags: data.tags,
        description: data.description,
        visibility: GqlVisibility.IsPublic,
      });
      const toast = await this.toastCtrl.create({
        message: 'Created',
        duration: 3000,
        color: 'success',
      });

      await toast.present();
    } else {
      const toast = await this.toastCtrl.create({
        message: 'Canceled',
        duration: 3000,
      });

      await toast.present();
    }
  }
}
