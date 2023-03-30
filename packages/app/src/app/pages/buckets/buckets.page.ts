import { Component } from '@angular/core';
import { ApolloClient } from '@apollo/client/core';
import {
  ActionSheetButton,
  ActionSheetController,
  ModalController,
  ToastController,
} from '@ionic/angular';
import {
  GqlContentCategoryTag,
  GqlVisibility,
} from '../../../generated/graphql';
import { Pagination } from '../../services/pagination.service';
import { BasicBucket, BucketService } from '../../services/bucket.service';
import { FilteredList } from '../../components/filtered-list';
import {
  FilterData,
  Filters,
} from '../../components/filter-toolbar/filter-toolbar.component';
import { ProfileService } from 'src/app/services/profile.service';
import { AuthService } from 'src/app/services/auth.service';
import { FormControl } from '@angular/forms';
import { BucketCreateModalComponent } from '../../modals/bucket-create-modal/bucket-create-modal.component';
import { enumToKeyValue, toOrderBy } from '../feeds/feeds.page';
import { OpmlService } from '../../services/opml.service';
import { debounce, DebouncedFunc } from 'lodash-es';
import { ImportModalComponent } from '../../modals/import-modal/import-modal.component';
import { visibilityToLabel } from './bucket/bucket.page';

interface BucketFilterValues {
  tag: GqlContentCategoryTag;
  visibility: GqlVisibility;
}

@Component({
  selector: 'app-buckets-page',
  templateUrl: './buckets.page.html',
  styleUrls: ['./buckets.page.scss'],
})
export class BucketsPage extends FilteredList<
  BasicBucket,
  FilterData<BucketFilterValues>
> {
  filters: Filters<BucketFilterValues> = {
    tag: {
      name: 'tag',
      control: new FormControl<GqlContentCategoryTag[]>([]),
      options: enumToKeyValue(GqlContentCategoryTag),
    },
    visibility: {
      name: 'visibility',
      control: new FormControl<GqlVisibility[]>([]),
      options: enumToKeyValue(GqlVisibility),
    },
  };
  optionsFormControl: FormControl = new FormControl<string>('');
  handleBucketActionDebounced: DebouncedFunc<any>;

  constructor(
    private readonly apollo: ApolloClient<any>,
    private readonly bucketService: BucketService,
    private readonly modalCtrl: ModalController,
    private readonly profileService: ProfileService,
    private readonly opmlService: OpmlService,
    private readonly authService: AuthService,
    private readonly toastCtrl: ToastController,
    readonly actionSheetCtrl: ActionSheetController
  ) {
    super('bucket', actionSheetCtrl);
    this.handleBucketActionDebounced = debounce(
      this.handleBucketAction.bind(this),
      300
    );
  }

  getBulkActionButtons(): ActionSheetButton<any>[] {
    return [];
  }

  fetch(
    filterData: FilterData<BucketFilterValues>,
    page: number
  ): Promise<[BasicBucket[], Pagination]> {
    return this.bucketService
      .search({
        page,
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
        await this.triggerFetch();
        break;
    }
  }

  async handleBucketAction(event: any): Promise<void> {
    if (await this.authService.isAuthenticatedOrRedirect()) {
      switch (event.detail.value) {
        case 'import':
          const modal = await this.modalCtrl.create({
            component: ImportModalComponent,
            showBackdrop: true,
          });
          await modal.present();
          break;
        case 'export':
          break;
      }
      event.stopPropagation();
      event.preventDefault();
      if (event.detail.value) {
        this.optionsFormControl.setValue('');
      }
    }
  }

  label(visibility: GqlVisibility): string {
    return visibilityToLabel(visibility);
  }

  isOwner(entity: BasicBucket): boolean {
    return this.profileService.getUserId() === entity.ownerId;
  }
}
