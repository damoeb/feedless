import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ActionSheetController } from '@ionic/angular';
import { Article, ArticleService } from '../../services/article.service';
import { Pagination } from '../../services/pagination.service';
import {
  FilterQuery,
  Filters,
} from '../filter-toolbar/filter-toolbar.component';
import { FilteredList } from '../filtered-list';
import { ActionSheetButton } from '@ionic/core/dist/types/components/action-sheet/action-sheet-interface';
import { FormControl } from '@angular/forms';
import {
  GqlArticleReleaseStatus,
  GqlArticleType,
  GqlContentCategoryTag,
  GqlContentTypeTag,
} from '../../../generated/graphql';

@Component({
  selector: 'app-articles',
  templateUrl: './articles.component.html',
  styleUrls: ['./articles.component.scss'],
})
export class ArticlesComponent
  extends FilteredList<Article, FilterQuery>
  implements OnInit
{
  @Input()
  streamId: string;
  @Input()
  name: string;
  filters: Filters = {
    tag: {
      name: 'tag',
      control: new FormControl<GqlContentCategoryTag[]>([]),
      options: Object.values(GqlContentCategoryTag),
    },
    content: {
      name: 'content',
      control: new FormControl<GqlContentTypeTag[]>(
        Object.values(GqlContentTypeTag)
      ),
      options: Object.values(GqlContentTypeTag),
    },
    status: {
      name: 'status',
      control: new FormControl<GqlArticleReleaseStatus[]>([
        GqlArticleReleaseStatus.Released,
      ]),
      options: Object.values(GqlArticleReleaseStatus),
    },
    type: {
      name: 'type',
      control: new FormControl<GqlArticleType[]>([GqlArticleType.Feed]),
      options: Object.values(GqlArticleType),
    },
  };

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly articleService: ArticleService,
    readonly actionSheetCtrl: ActionSheetController
  ) {
    super('article', actionSheetCtrl);
  }

  ngOnInit(): void {
    this.entityName = this.name;
  }

  async fetch(filterData: FilterQuery): Promise<[Article[], Pagination]> {
    const response = await this.articleService.findAllByStreamId(
      this.streamId,
      this.currentPage,
      filterData.query,
      filterData.articleType,
      filterData.releaseStatus
    );
    return [response.articles, response.pagination];
  }

  getBulkActionButtons(): ActionSheetButton[] {
    return [
      {
        text: 'Delete',
        role: 'destructive',
        handler: () => {},
      },
      {
        text: 'Forward',
        role: 'destructive',
        handler: () => {},
      },
      {
        text: 'Publish',
        role: 'destructive',
        handler: () => {},
      },
      {
        text: 'Retract',
        role: 'destructive',
        handler: () => {},
      },
    ];
  }
}
