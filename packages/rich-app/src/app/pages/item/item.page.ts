import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ArticleService } from '../../services/article.service';
import { GqlArticle } from '../../../generated/graphql';

@Component({
  selector: 'app-item',
  templateUrl: './item.page.html',
  styleUrls: ['./item.page.scss'],
})
export class ItemPage implements OnInit {
  public article: GqlArticle;
  loading: boolean;
  content: string;
  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly articleService: ArticleService
  ) {}

  ngOnInit() {
    this.activatedRoute.queryParams.subscribe((queryParams) => {
      console.log(`url ` + queryParams.url);
    });
    this.activatedRoute.params.subscribe((params) => {
      console.log(`articleId ` + params.id);
      if (params.id) {
        this.articleService.findById(params.id).subscribe((response) => {
          this.article = response.data.article;
          this.content = this.article.content_text;
        });
      }
    });
  }
}
