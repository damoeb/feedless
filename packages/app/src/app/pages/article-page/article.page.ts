import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ActualArticle, ArticleService } from '../../services/article.service';
import { ActualBucket, BucketService } from '../../services/bucket.service';

@Component({
  selector: 'app-bucket',
  templateUrl: './article.page.html',
  styleUrls: ['./article.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ArticlePage implements OnInit {
  loadingBucket: boolean;
  loadingArticle: boolean;
  bucket: ActualBucket;
  renderFulltext = false;
  private article: ActualArticle;
  constructor(private readonly activatedRoute: ActivatedRoute,
              private readonly changeRef: ChangeDetectorRef,
              private readonly articleService: ArticleService,
              private readonly bucketService: BucketService) {}

  ngOnInit() {
    this.activatedRoute.params.subscribe((params) => {
      Promise.all([
        this.initBucket(params.id),
        this.initArticle(params.articleId)
      ]).then(() => {
        this.changeRef.detectChanges();
      });
    });
  }

  private async initBucket(bucketId: string) {
    console.log('initBucket', bucketId)
    this.loadingBucket = true;
    try {
      this.bucket = await this.bucketService.getBucketById(bucketId);
    } finally {
      this.loadingBucket = false;
    }
  }

  private async initArticle(articleId: string) {
    console.log('initArticle', articleId)
    this.loadingArticle = true;
    try {
      this.article = await this.articleService.findById(articleId);
    } finally {
      this.loadingArticle = false;
    }

  }

  toggleFulltext(event: any) {
    this.renderFulltext = event.detail.checked;
    this.changeRef.detectChanges();
  }
}
