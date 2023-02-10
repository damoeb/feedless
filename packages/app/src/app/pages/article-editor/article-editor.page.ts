import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  OnInit,
  ViewChild,
  ViewEncapsulation,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import {
  ArticleService,
  ArticleWithContext,
  BasicContext,
} from '../../services/article.service';
import { ModalController } from '@ionic/angular';
import {
  ImportArticleComponent,
  ImportArticleComponentProps,
} from '../../components/import-article/import-article.component';
import { SettingsService } from '../../services/settings.service';
import { ModalDismissal } from '../../app.module';
import { Bucket } from '../../services/bucket.service';
import { BasicNativeFeed } from '../../services/feed.service';
import { PlayerService } from '../../services/player.service';

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
    private readonly settingsService: SettingsService,
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
