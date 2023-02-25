import { Component, Input, OnInit } from '@angular/core';
import { Article } from '../../services/article.service';
import { GqlReleaseStatus } from '../../../generated/graphql';

@Component({
  selector: 'app-article-editor',
  templateUrl: './article-editor.component.html',
  styleUrls: ['./article-editor.component.scss'],
})
export class ArticleEditorComponent implements OnInit {
  @Input()
  article: Article;
  releaseStatusList = Object.values(GqlReleaseStatus);

  constructor() {}

  async ngOnInit() {}
}
