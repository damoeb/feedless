import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ModalController } from '@ionic/angular';
import { BucketService } from '../../services/bucket.service';
import { GqlArticle, GqlBucket } from '../../../generated/graphql';
import { BucketSettingsComponent } from '../../components/bucket-settings/bucket-settings.component';

@Component({
  selector: 'app-bucket-page',
  templateUrl: './bucket.page.html',
  styleUrls: ['./bucket.page.scss'],
})
export class BucketPage implements OnInit {
  public bucket: GqlBucket;
  public articles: GqlArticle[];

  constructor(
    private activatedRoute: ActivatedRoute,
    private bucketService: BucketService,
    private modalController: ModalController
  ) {}

  ngOnInit() {
    const bucketId = this.activatedRoute.snapshot.paramMap.get('id');
    this.bucketService.getBucketsById(bucketId).subscribe((response) => {
      this.bucket = response.data.bucket;
    });
    this.bucketService.getArticlesInBuckets(bucketId).subscribe((response) => {
      this.articles = response.data.articles;
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
