import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Bucket, BucketService } from '../../../services/bucket.service';
import { ModalController, ToastController } from '@ionic/angular';
import { ModalDismissal } from '../../../app.module';
import {
  SubscribeModalComponent,
  SubscribeModalComponentProps,
} from '../../../modals/subscribe-modal/subscribe-modal.component';
import { FetchPolicy } from '@apollo/client/core';
import {
  BucketCreateModalComponent,
  BucketCreateModalComponentProps,
} from '../../../modals/bucket-create-modal/bucket-create-modal.component';
import { GqlVisibility } from '../../../../generated/graphql';
import { ServerSettingsService } from '../../../services/server-settings.service';
import { FilterData } from '../../../components/filter-toolbar/filter-toolbar.component';
import { ArticlesFilterValues } from '../../../components/articles/articles.component';

@Component({
  selector: 'app-bucket-page',
  templateUrl: './bucket.page.html',
  styleUrls: ['./bucket.page.scss'],
})
export class BucketPage implements OnInit {
  loadingBucket: boolean;
  bucket: Bucket;
  query = '';
  showArticles = true;
  filterData: FilterData<ArticlesFilterValues>;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly router: Router,
    private readonly toastCtrl: ToastController,
    private readonly bucketService: BucketService,
    private readonly serverSettingsService: ServerSettingsService,
    private readonly modalCtrl: ModalController
  ) {}

  ngOnInit() {
    this.activatedRoute.params.subscribe((params) => {
      this.fetchBucket(params.id);
    });
  }

  async editBucket() {
    const componentProps: BucketCreateModalComponentProps = {
      bucket: this.bucket,
    };
    const modal = await this.modalCtrl.create({
      component: BucketCreateModalComponent,
      componentProps,
      showBackdrop: true,
    });
    await modal.present();
    const { data, role } = await modal.onDidDismiss();

    if (data) {
      await this.bucketService.updateBucket({
        data: {
          name: {
            set: data.title,
          },
          description: {
            set: data.description,
          },
          websiteUrl: {
            set: data.websiteUrl,
          },
          imageUrl: {
            set: data.imageUrl,
          },
          // tags: {
          //   set: data.tags
          // }
        },
        where: {
          id: this.bucket.id,
        },
      });
      const toast = await this.toastCtrl.create({
        message: 'Updated',
        duration: 3000,
        color: 'success',
      });
      await toast.present();
      await this.fetchBucket(this.bucket.id, 'network-only');
    } else {
      const toast = await this.toastCtrl.create({
        message: 'Canceled',
        duration: 3000,
      });

      await toast.present();
    }
  }

  async openSubscribeModal() {
    const feedUrl = `${this.serverSettingsService.publicUrl}/bucket:${this.bucket.id}`;
    const componentProps: SubscribeModalComponentProps = {
      jsonFeedUrl: `${feedUrl}/json`,
      atomFeedUrl: `${feedUrl}/atom`,
      filter: this.filterData,
    };
    const modal = await this.modalCtrl.create({
      component: SubscribeModalComponent,
      componentProps,
    });
    await modal.present();
    await modal.onDidDismiss<ModalDismissal>();
  }

  async handleBucketAction(event: any) {
    switch (event.detail.value) {
      case 'edit':
        await this.editBucket();
        break;
      case 'delete':
        await this.bucketService.deleteBucket(this.bucket.id);
        const toast = await this.toastCtrl.create({
          message: 'Deleted',
          duration: 3000,
          color: 'success',
        });
        await toast.present();
        await this.router.navigateByUrl('/buckets');
        break;
    }
  }

  label(visibility: GqlVisibility): string {
    switch (visibility) {
      case GqlVisibility.IsPrivate:
        return 'private';
      case GqlVisibility.IsProtected:
        return 'protected';
      case GqlVisibility.IsPublic:
        return 'public';
    }
  }

  private async fetchBucket(
    bucketId: string,
    fetchPolicy: FetchPolicy = 'cache-first'
  ) {
    this.loadingBucket = true;
    try {
      this.bucket = await this.bucketService.getBucketById(
        bucketId,
        fetchPolicy
      );
    } finally {
      this.loadingBucket = false;
    }
  }
}