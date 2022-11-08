import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Article, ArticleService } from '../../services/article.service';

@Component({
  selector: 'app-bucket',
  templateUrl: './article.page.html',
  styleUrls: ['./article.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ArticlePage implements OnInit {
  loadingArticle: boolean;
  renderFulltext = false;
  private article: Article;
  constructor(private readonly activatedRoute: ActivatedRoute,
              private readonly changeRef: ChangeDetectorRef,
              private readonly articleService: ArticleService) {}

  ngOnInit() {
    this.activatedRoute.params.subscribe((params) => {
      Promise.all([
        this.initArticle(params.articleId)
      ]).then(() => {
        this.changeRef.detectChanges();
      });
    });
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
