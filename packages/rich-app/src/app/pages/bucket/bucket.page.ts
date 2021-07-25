import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ModalController } from '@ionic/angular';
import { BucketService } from '../../services/bucket.service';
import {
  GqlArticle,
  GqlArticleRef,
  GqlBucket,
} from '../../../generated/graphql';
import { BucketSettingsComponent } from '../../components/bucket-settings/bucket-settings.component';
import { StreamService } from '../../services/stream.service';

@Component({
  selector: 'app-bucket-page',
  templateUrl: './bucket.page.html',
  styleUrls: ['./bucket.page.scss'],
})
export class BucketPage implements OnInit {
  public bucket: GqlBucket;
  public articleRefs: GqlArticleRef[];

  constructor(
    private activatedRoute: ActivatedRoute,
    private bucketService: BucketService,
    private streamService: StreamService,
    private modalController: ModalController
  ) {}

  ngOnInit() {
    const bucketId = this.activatedRoute.snapshot.paramMap.get('id');
    this.bucketService.getBucketsById(bucketId).subscribe((response) => {
      this.bucket = response.data.bucket;
      this.streamService
        .getArticles(this.bucket.streamId)
        .subscribe((response) => {
          this.articleRefs = response.data.articleRefs;
        });
    });
  }

  async showSettings() {
    const modal = await this.modalController.create({
      component: BucketSettingsComponent,
      componentProps: {
        bucket: this.bucket,
      },
    });

    await modal.present();
  }
}
