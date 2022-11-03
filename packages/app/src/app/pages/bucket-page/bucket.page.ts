import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ArticleFromFeed, ArticleService } from '../../services/article.service';
import { ActualBucket, BucketService } from '../../services/bucket.service';
import { ActualPagination } from '../../services/pagination.service';
import { GqlArticleType, GqlReleaseStatus } from '../../../generated/graphql';

@Component({
  selector: 'app-bucket',
  templateUrl: './bucket.page.html',
  styleUrls: ['./bucket.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BucketPage implements OnInit {
  loadingBucket: boolean;
  bucket: ActualBucket;
  currentPage: number = 0;
  articles: Array<ArticleFromFeed>;
  pagination: ActualPagination;
  renderFulltext = false;
  constructor(private readonly activatedRoute: ActivatedRoute,
              private readonly changeRef: ChangeDetectorRef,
              private readonly articleService: ArticleService,
              private readonly bucketService: BucketService) {}

  ngOnInit() {
    this.activatedRoute.params.subscribe(params => {
      this.initBucket(params.id);
    })
  }

  private async initBucket(bucketId: string) {
    console.log('initBucket', bucketId)
    this.loadingBucket = true;
    try {
      this.bucket = await this.bucketService.getBucketById(bucketId);
    } finally {
      this.loadingBucket = false;
    }
    console.log('this.bucket', this.bucket)
    this.changeRef.detectChanges();

    this.fetchArticles();
  }

  private async fetchArticles() {
    const response = await this.articleService.findAllByStreamId(this.bucket.streamId, this.currentPage, GqlArticleType.Feed, GqlReleaseStatus.Released);
    this.articles = response.articles;
    this.pagination = response.pagination;
    this.changeRef.detectChanges();
  }

  toggleFulltext(event: any) {
    this.renderFulltext = event.detail.checked;
    this.changeRef.detectChanges();
  }

  lastUpdatedAt(): Date {
    return new Date(this.bucket.lastUpdatedAt)
  }
}
