import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Article, ArticleService } from '../../services/article.service';
import { Bucket, BucketService } from '../../services/bucket.service';
import { Pagination } from '../../services/pagination.service';
import { GqlArticleType, GqlReleaseStatus } from '../../../generated/graphql';
import { ActionSheetController, InfiniteScrollCustomEvent } from '@ionic/angular';
import { without } from 'lodash';

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
  articles: Array<Article> = [];
  checkedArticles: Array<Article> = [];
  pagination: Pagination;
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

  lastUpdatedAt(): Date {
    return new Date(this.bucket.lastUpdatedAt);
  }

  async loadMoreArticles(event: InfiniteScrollCustomEvent) {
    if (!this.pagination.isLast) {
      this.currentPage++;
      await this.fetchArticles();
      await event.target.complete();
    }
  }

  async showOptions() {
    const actionSheet = await this.actionSheetCtrl.create({
      header: 'Actions for Bucket',
      buttons: [
        {
          text: 'Add Article',
          role: 'destructive',
          handler: () => {
          },
        },
        {
          text: 'Edit',
          role: 'destructive',
          handler: () => {
            this.router.navigateByUrl(location.pathname + '/edit');
          },
        },
        {
          text: 'Delete',
          role: 'destructive',
          handler: () => {
            this.deleteBucket();
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

  async showActions() {
    const actionSheet = await this.actionSheetCtrl.create({
      header: `Actions for ${this.checkedArticles.length} Articles`,
      buttons: [
        {
          text: 'Delete',
          role: 'destructive',
          handler: () => {
          },
        },
        {
          text: 'Publish',
          role: 'destructive',
          handler: () => {
          },
        },
      ],
    });

    await actionSheet.present();

    const result = await actionSheet.onDidDismiss();
  }

  post() {

  }

  onCheckChange(isChecked: boolean, article: Article) {
    if (isChecked) {
      this.checkedArticles.push(article);
    } else {
      this.checkedArticles = without(this.checkedArticles, article);
    }
    console.log(this.checkedArticles);
  }

  toggleCheckAll(event: any) {
    if (event.detail.checked) {
      this.checkedArticles = [...this.articles];
    } else {
      this.checkedArticles = [];
    }
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

  private async deleteBucket() {
    await this.bucketService.deleteBucket(this.bucket.id);
    await this.router.navigateByUrl('/');
  }
}
