import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Bucket, BucketService } from '../../../services/bucket.service';
import { ModalController, ToastController } from '@ionic/angular';
import { ModalDismissal } from '../../../app.module';
import { SubscribeModalComponent } from '../../../modals/subscribe-modal/subscribe-modal.component';
import { FetchPolicy } from '@apollo/client/core';

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

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly router: Router,
    private readonly toastCtrl: ToastController,
    private readonly bucketService: BucketService,
    private readonly modalCtrl: ModalController
  ) {}

  ngOnInit() {
    this.activatedRoute.params.subscribe((params) => {
      this.fetchBucket(params.id);
    });
  }

  async editBucket() {
    const data = await this.bucketService.showBucketAlert(
      'Edit Bucket',
      this.bucket
    );
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
    const modal = await this.modalCtrl.create({
      component: SubscribeModalComponent,
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
