import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ModalController } from '@ionic/angular';
import { BucketService } from '../../services/bucket.service';
import { GqlArticleRef, GqlBucket } from '../../../generated/graphql';
import { BucketSettingsComponent } from '../../components/bucket-settings/bucket-settings.component';
import { StreamService } from '../../services/stream.service';
import { ToastService } from '../../services/toast.service';
import * as timeago from 'timeago.js';

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
  hasMore = true;

  constructor(
    private activatedRoute: ActivatedRoute,
    private bucketService: BucketService,
    private toastService: ToastService,
    private streamService: StreamService,
    private router: Router,
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
      .then(({ data, error }) => {
        if (error) {
          this.toastService.errorFromApollo(error);
        } else {
          this.bucket = data.bucket;

          const modal = this.activatedRoute.snapshot.paramMap.get('modal');
          if (modal === 'edit') {
            this.showSettings();
          }

          return this.fetchNextPage();
        }
      });
    // .catch((e) => {
    //   this.error = true;
    //   return this.toastService.errorFromApollo(e);
    // })
    // .finally(() => {
    //   this.loading = false;
    // });
  }

  async showSettings() {
    const modal = await this.modalController.create({
      component: BucketSettingsComponent,
      componentProps: {
        bucket: this.bucket,
      },
    });

    await modal.present();
    const response = await modal.onDidDismiss<boolean>();
    if (response.data) {
      // todo mag await this.router.navigate([`/bucket/${this.bucket.id}`]);
      this.reload();
    }
  }

  bucketNeedsAction(): boolean {
    return this.bucket?.subscriptions.some((s) => s.feed.broken);
  }

  getFeedUrl(): string {
    // todo mag bucket feed url
    return `/bucket/${this.bucket?.id}/atom`;
  }

  fetchNextPage(): Promise<Page> {
    if (!this.hasMore) {
      return;
    }
    this.loading = true;
    return this.streamService
      .getArticles(
        this.bucket.streamId,
        this.currentPage * this.take,
        this.take
      )
      .toPromise()
      .then((response) => {
        const page = {
          page: this.currentPage,
          articleRefs: response.data.articleRefs,
        };
        if (page.articleRefs.length === 0) {
          this.hasMore = false;
        } else {
          this.pages.push(page);
        }
        this.currentPage++;
        return page;
      })
      .finally(() => {
        this.loading = false;
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

  getLastUpdatedAt(date: Date) {
    if (date) {
      return timeago.format(date);
    }
  }
}
