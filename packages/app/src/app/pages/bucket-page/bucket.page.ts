import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
  ArticleMatch,
  ArticleService,
} from '../../services/article.service';
import { Bucket, BucketService } from '../../services/bucket.service';
import { Pagination } from '../../services/pagination.service';
import { GqlArticleType, GqlReleaseStatus } from '../../../generated/graphql';
import { ActionSheetController } from '@ionic/angular';

@Component({
  selector: 'app-bucket',
  templateUrl: './bucket.page.html',
  styleUrls: ['./bucket.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BucketPage implements OnInit {
  loadingBucket: boolean;
  bucket: Bucket;
  currentPage = 0;
  articles: Array<ArticleMatch> = [];
  pagination: Pagination;
  renderFulltext = false;
  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly changeRef: ChangeDetectorRef,
    private readonly router: Router,
    private readonly actionSheetCtrl: ActionSheetController,
    private readonly articleService: ArticleService,
    private readonly bucketService: BucketService
  ) {}

  ngOnInit() {
    this.activatedRoute.params.subscribe((params) => {
      this.initBucket(params.id);
    });
  }

  private async initBucket(bucketId: string) {
    this.loadingBucket = true;
    try {
      this.bucket = await this.bucketService.getBucketById(bucketId);
    } finally {
      this.loadingBucket = false;
    }
    console.log('this.bucket', this.bucket);
    this.changeRef.detectChanges();

    this.fetchArticles();
  }

  private async fetchArticles() {
    const response = await this.articleService.findAllByStreamId(
      this.bucket.streamId,
      this.currentPage,
      [GqlArticleType.Feed],
      [GqlReleaseStatus.Released]
    );
    this.articles.push(...response.articles);
    this.pagination = response.pagination;
    this.changeRef.detectChanges();
  }

  toggleFulltext(event: any) {
    this.renderFulltext = event.detail.checked;
    this.changeRef.detectChanges();
  }

  lastUpdatedAt(): Date {
    return new Date(this.bucket.lastUpdatedAt);
  }

  loadMoreArticles() {
    if (!this.pagination.isLast) {
      this.currentPage++;
      this.fetchArticles();
    }
  }

  async showOptions() {
    const actionSheet = await this.actionSheetCtrl.create({
      buttons: [
        {
          text: 'Edit',
          role: 'destructive',
          handler: () => {
            this.router.navigateByUrl(location.pathname + '/edit');
          },
        },
        {
          text: 'Cancel',
          role: 'cancel',
          data: {
            action: 'cancel',
          },
        },
      ],
    });

    await actionSheet.present();

    const result = await actionSheet.onDidDismiss();
  }
}
