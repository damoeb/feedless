import { Component } from '@angular/core';
import { ApolloClient } from '@apollo/client/core';
import { ActionSheetButton, ActionSheetController, ModalController, ToastController } from '@ionic/angular';
import { GqlContentCategoryTag, GqlHealth, GqlSortOrder, GqlVisibility } from '../../../generated/graphql';
import { Pagination } from '../../services/pagination.service';
import { BasicBucket, BucketService } from '../../services/bucket.service';
import { FilteredList } from '../../components/filtered-list';
import { Filters, FilterData } from '../../components/filter-toolbar/filter-toolbar.component';
import { ProfileService } from 'src/app/services/profile.service';
import { AuthService } from 'src/app/services/auth.service';
import { FormControl } from '@angular/forms';
import { BucketCreateModalComponent } from '../../modals/bucket-create-modal/bucket-create-modal.component';
import { enumToMap, toOrderBy } from '../feeds/feeds.page';

export interface FeedFilterValues {
  tag: GqlContentCategoryTag;
  visibility: GqlVisibility;
  health: GqlHealth;
}

@Component({
  selector: 'app-buckets-page',
  templateUrl: './buckets.page.html',
  styleUrls: ['./buckets.page.scss'],
})
export class BucketsPage extends FilteredList<BasicBucket, FilterData<FeedFilterValues>> {
  filters: Filters<FeedFilterValues> = {
    tag: {
      name: 'tag',
      control: new FormControl<GqlContentCategoryTag[]>([]),
      options: enumToMap(GqlContentCategoryTag),
    },
    visibility: {
      name: 'visibility',
      control: new FormControl<GqlVisibility[]>([]),
      options: enumToMap(GqlVisibility),
    },
    health: {
      name: 'health',
      control: new FormControl<GqlHealth[]>([]),
      options: enumToMap(GqlHealth),
    },
  };
  constructor(
    private readonly apollo: ApolloClient<any>,
    private readonly bucketService: BucketService,
    private readonly modalCtrl: ModalController,
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

  fetch(filterData: FilterData<FeedFilterValues>): Promise<[BasicBucket[], Pagination]> {
    console.log('filterData', filterData);
    return this.bucketService
      .search({
        page: 0,
        where: {
          query: '',
        },
        orderBy: toOrderBy(filterData.sortBy),
      })
      .then((response) => [response.buckets, response.pagination]);
  }

  toDate(createdAt: number): Date {
    return new Date(createdAt);
  }

  async showCreateBucketModal() {
    const modal = await this.modalCtrl.create({
      component: BucketCreateModalComponent,
      showBackdrop: true,
    });
    await modal.present();
    const { data, role } = await modal.onDidDismiss();

    switch (role) {
      case 'save':
        await this.bucketService.createBucket({
          title: data.title,
          websiteUrl: data.websiteUrl,
          imageUrl: data.imageUrl,
          tags: data.tags,
          description: data.description,
          visibility: GqlVisibility.IsPublic,
        });
        const toast = await this.toastCtrl.create({
          message: 'Bucket created',
          duration: 3000,
          color: 'success',
        });

        await toast.present();
        break;
    }
  }
}
