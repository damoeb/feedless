import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ArticleService } from '../../services/article.service';
import { GqlArticleRef } from '../../../generated/graphql';

@Component({
  selector: 'app-item',
  templateUrl: './read.page.html',
  styleUrls: ['./read.page.scss'],
})
export class ReadPage implements OnInit {
  articleRef: GqlArticleRef;
  loading: boolean;
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
          this.articleRef = response.data.findFirstArticleRef;
        });
      }
    });
  }
}
