import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
  ViewEncapsulation,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import {
  ArticleService,
  ArticleWithContext,
  BasicContext,
} from '../../services/article.service';
import { Bucket } from '../../services/bucket.service';
import { BasicNativeFeed } from '../../services/feed.service';

@Component({
  selector: 'app-editor-page',
  templateUrl: './article-editor.page.html',
  styleUrls: ['./article-editor.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None,
})
export class ArticleEditorPage implements OnInit {
  locale = 'de-AT';

  loading: boolean;

  article: ArticleWithContext;
  bucketId: string;
  bucket: Bucket;
  nativeFeed: BasicNativeFeed;
  context: BasicContext;

  private articleId: string;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly changeRef: ChangeDetectorRef,
    private readonly articleService: ArticleService
  ) {}

  ngOnInit() {
    this.activatedRoute.params.subscribe((params) => {
      this.bucketId = params.id;
      this.articleId = params.articleId;
      this.init(params.articleId).finally(() => {
        this.loading = false;
        this.changeRef.detectChanges();
      });
    });
  }

  private async init(articleId: string) {
    this.loading = true;
    try {
      this.article = await this.articleService.findById(articleId);
      this.bucket = this.article.bucket;
      this.nativeFeed = this.article.nativeFeed;
      this.context = this.article.context;
      this.loading = false;
      this.changeRef.detectChanges();
    } finally {
      this.loading = false;
      this.changeRef.detectChanges();
    }
  }
}
