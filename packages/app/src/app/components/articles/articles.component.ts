import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ActionSheetController } from '@ionic/angular';
import { Article, ArticleService } from '../../services/article.service';
import { Pagination } from '../../services/pagination.service';
import {
  FilterData,
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
import { enumToMap, toOrderBy } from '../../pages/feeds/feeds.page';

export interface ArticlesFilterValues {
  tag: GqlContentCategoryTag;
  content: GqlContentTypeTag;
  status: GqlArticleReleaseStatus;
  type: GqlArticleType;
}

export const articleFilters: Filters<ArticlesFilterValues> = {
  tag: {
    name: 'tag',
    control: new FormControl<GqlContentCategoryTag[]>([]),
    options: enumToMap(GqlContentCategoryTag),
  },
  content: {
    name: 'content',
    control: new FormControl<GqlContentTypeTag[]>(
      Object.keys(GqlContentTypeTag) as GqlContentTypeTag[]
    ),
    options: enumToMap(GqlContentTypeTag),
  },
  status: {
    name: 'status',
    control: new FormControl<GqlArticleReleaseStatus[]>([
      GqlArticleReleaseStatus.Released,
      GqlArticleReleaseStatus.Dropped,
      GqlArticleReleaseStatus.Unreleased,
    ]),
    options: enumToMap(GqlArticleReleaseStatus),
  },
  type: {
    name: 'type',
    control: new FormControl<GqlArticleType[]>([GqlArticleType.Feed]),
    options: enumToMap(GqlArticleType),
  },
};

@Component({
  selector: 'app-articles',
  templateUrl: './articles.component.html',
  styleUrls: ['./articles.component.scss'],
})
export class ArticlesComponent
  extends FilteredList<Article, FilterData<ArticlesFilterValues>>
  implements OnInit
{
  @Input()
  streamId: string;
  @Input()
  name: string;
  @Output()
  filterChange: EventEmitter<FilterData<ArticlesFilterValues>> =
    new EventEmitter<FilterData<ArticlesFilterValues>>();

  filters: Filters<ArticlesFilterValues> = articleFilters;

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

  async fetch(
    filterData: FilterData<ArticlesFilterValues>,
    page: number
  ): Promise<[Article[], Pagination]> {
    const response = await this.articleService.findAllByStreamId({
      page,
      where: {
        streamId: this.streamId,
        status: {
          oneOf: filterData.filters.status,
        },
        type: {
          oneOf: filterData.filters.type,
        },
      },
      orderBy: toOrderBy(filterData.sortBy),
    });
    return [response.articles, response.pagination];
  }

  getBulkActionButtons(): ActionSheetButton[] {
    return [
      {
        text: 'Delete',
        role: 'destructive',
        handler: () => this.deleteSelectedArticles(),
      },
      // {
      //   text: 'Forward',
      //   role: 'destructive',
      //   handler: () => {},
      // },
      {
        text: 'Publish',
        role: 'destructive',
        handler: () => this.updateSelectedArticlesStatus(GqlArticleReleaseStatus.Released),
      },
      {
        text: 'Retract',
        role: 'destructive',
        handler: () => this.updateSelectedArticlesStatus(GqlArticleReleaseStatus.Unreleased),
      },
    ];
  }

  private async deleteSelectedArticles() {
    await this.articleService.deleteArticles({
      where: {
        in: this.checkedEntities.map(a => ({ id: a.id }))
      }
    });
  }

  private async updateSelectedArticlesStatus(status: GqlArticleReleaseStatus) {
    await this.articleService.updateArticles({
      where: {
        in: this.checkedEntities.map(a => ({ id: a.id }))
      },
      data: {
        status: {
          set: status
        }
      }
    });
  }
}
