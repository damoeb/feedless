import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ModalController } from '@ionic/angular';
import { BucketService } from '../../services/bucket.service';
import { GqlArticleRef, GqlBucket } from '../../../generated/graphql';
import { BucketSettingsComponent } from '../../components/bucket-settings/bucket-settings.component';
import { StreamService } from '../../services/stream.service';

interface Page {
  page: number;
  articleRefs: GqlArticleRef[];
}

@Component({
  selector: 'app-bucket-page',
  templateUrl: './bucket.page.html',
  styleUrls: ['./bucket.page.scss'],
})
export class BucketPage implements OnInit {
  public bucket: GqlBucket = null;
  showTags = true;
  loading = false;
  pages: Page[] = [];
  private currentPage = 0;
  private take = 10;

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
    this.bucketService
      .getBucketsById(bucketId)
      .toPromise()
      .then((response) => {
        this.bucket = response.data.bucket;
        return this.fetchNextPage();
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

  getFeedUrl(): string {
    return '';
  }

  fetchNextPage(): Promise<Page> {
    return this.streamService
      .getArticles(
        this.bucket.streamId,
        this.currentPage * this.take,
        this.take
      )
      .toPromise()
      .then((response) => {
        let page = {
          page: this.currentPage,
          articleRefs: response.data.articleRefs,
        };
        this.pages.push(page);
        this.currentPage++;
        this.loading = false;
        return page;
      });
  }

  loadNextPage(event) {
    console.log('loadNextPage');
    return this.fetchNextPage()
      .then((page) => {
        event.target.disabled = page.articleRefs.length === 0;
      })
      .finally(() => {
        event.target.complete();
      });
  }
}
