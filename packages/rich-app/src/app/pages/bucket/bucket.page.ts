import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ModalController } from '@ionic/angular';
import { BucketService } from '../../services/bucket.service';
import { GqlArticleRef, GqlBucket } from '../../../generated/graphql';
import { BucketSettingsComponent } from '../../components/bucket-settings/bucket-settings.component';
import { StreamService } from '../../services/stream.service';

@Component({
  selector: 'app-bucket-page',
  templateUrl: './bucket.page.html',
  styleUrls: ['./bucket.page.scss'],
})
export class BucketPage implements OnInit {
  public bucket: GqlBucket = null;
  public articleRefs: GqlArticleRef[] = [];
  showTags = true;
  loading = false;

  constructor(
    private activatedRoute: ActivatedRoute,
    private bucketService: BucketService,
    private streamService: StreamService,
    private modalController: ModalController
  ) {}

  ngOnInit() {
    this.reload();
  }

  reload() {
    this.loading = true;
    const bucketId = this.activatedRoute.snapshot.paramMap.get('id');
    this.bucketService.getBucketsById(bucketId).subscribe((response) => {
      this.bucket = response.data.bucket;
      this.streamService
        .getArticles(this.bucket.streamId)
        .subscribe((response) => {
          this.articleRefs = response.data.articleRefs;
          this.loading = false;
        });
    });
  }

  async showSettings() {
    const modal = await this.modalController.create({
      component: BucketSettingsComponent,
      backdropDismiss: false,
      componentProps: {
        bucket: this.bucket,
      },
    });

    await modal.present();
    await modal.onDidDismiss();
    this.reload();
  }

  bucketNeedsAction(): boolean {
    return this.bucket?.subscriptions.some((s) => s.feed.broken);
  }
}
