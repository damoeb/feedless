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
import { enumToMap } from '../../pages/feeds/feeds.page';

export interface ArticlesFilterValues {
  tag: GqlContentCategoryTag;
  content: GqlContentTypeTag;
  status: GqlArticleReleaseStatus;
  type: GqlArticleType;
}

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

  filters: Filters<ArticlesFilterValues> = {
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
      ]),
      options: enumToMap(GqlArticleReleaseStatus),
    },
    type: {
      name: 'type',
      control: new FormControl<GqlArticleType[]>([GqlArticleType.Feed]),
      options: enumToMap(GqlArticleType),
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

  async fetch(
    filterData: FilterData<ArticlesFilterValues>
  ): Promise<[Article[], Pagination]> {
    const response = await this.articleService.findAllByStreamId(
      this.streamId,
      this.currentPage,
      '',
      filterData.filters.type,
      filterData.filters.status
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
