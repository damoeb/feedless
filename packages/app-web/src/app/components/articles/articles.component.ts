import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ActionSheetController } from '@ionic/angular';
import { ArticleService } from '../../services/article.service';
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
import { enumToKeyValue, toOrderBy } from '../../pages/feeds/feeds.page';
import { Article, Pagination } from '../../graphql/types';
import { FetchPolicy } from '@apollo/client/core';

export interface ArticlesFilterValues {
  tag: GqlContentCategoryTag;
  content: GqlContentTypeTag;
  status: GqlArticleReleaseStatus;
  type: GqlArticleType;
}

export const articleFilters = (
  isOwner: boolean,
): Filters<ArticlesFilterValues> => ({
  tag: {
    name: 'tag',
    control: new FormControl<GqlContentCategoryTag[]>([]),
    options: enumToKeyValue(GqlContentCategoryTag),
  },
  content: {
    name: 'content',
    control: new FormControl<GqlContentTypeTag[]>(
      Object.keys(GqlContentTypeTag) as GqlContentTypeTag[],
    ),
    options: enumToKeyValue(GqlContentTypeTag),
  },
  status: {
    name: 'status',
    control: new FormControl<GqlArticleReleaseStatus[]>(
      isOwner
        ? [
            GqlArticleReleaseStatus.Released,
            GqlArticleReleaseStatus.Dropped,
            GqlArticleReleaseStatus.Unreleased,
          ]
        : [GqlArticleReleaseStatus.Released],
    ),
    options: enumToKeyValue(GqlArticleReleaseStatus),
  },
  type: {
    name: 'type',
    control: new FormControl<GqlArticleType[]>(
      isOwner
        ? [GqlArticleType.Feed, GqlArticleType.Ops]
        : [GqlArticleType.Feed],
    ),
    options: enumToKeyValue(GqlArticleType),
  },
});

@Component({
  selector: 'app-articles',
  templateUrl: './articles.component.html',
  styleUrls: ['./articles.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ArticlesComponent
  extends FilteredList<Article, FilterData<ArticlesFilterValues>>
  implements OnInit, OnDestroy
{
  @Input()
  streamId: string;
  @Input()
  name: string;
  @Input()
  isOwner: boolean;
  @Output()
  filterChange: EventEmitter<FilterData<ArticlesFilterValues>> =
    new EventEmitter<FilterData<ArticlesFilterValues>>();

  filters: Filters<ArticlesFilterValues>;
  refreshIntervalId: any;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly articleService: ArticleService,
    private readonly changeRef: ChangeDetectorRef,
    readonly actionSheetCtrl: ActionSheetController,
  ) {
    super(actionSheetCtrl);
  }

  ngOnDestroy(): void {
    if (this.refreshIntervalId) {
      clearInterval(this.refreshIntervalId);
    }
  }

  ngOnInit(): void {
    this.filters = articleFilters(this.isOwner);
    let attempts = 30;
    this.refreshIntervalId = setInterval(async () => {
      if (attempts-- > 0 && this.entities.length === 0) {
        await this.refetch('network-only');
      } else {
        clearInterval(this.refreshIntervalId);
        this.refreshIntervalId = undefined;
        this.changeRef.detectChanges();
      }
    }, 1000);
  }

  async fetch(
    filterData: FilterData<ArticlesFilterValues>,
    page: number,
    fetchPolicy: FetchPolicy,
  ): Promise<[Article[], Pagination]> {
    const response = await this.articleService.findAllByStreamId(
      {
        cursor: {
          page,
        },
        where: {
          stream: {
            id: {
              equals: this.streamId,
            },
          },
          status: {
            oneOf: filterData.filters.status,
          },
          type: {
            oneOf: filterData.filters.type,
          },
        },
        orderBy: toOrderBy(filterData.sortBy),
      },
      fetchPolicy,
    );
    return [response.articles, response.pagination];
  }

  onDidChange() {
    this.changeRef.detectChanges();
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
        handler: () =>
          this.updateSelectedArticlesStatus(GqlArticleReleaseStatus.Released),
      },
      {
        text: 'Retract',
        role: 'destructive',
        handler: () =>
          this.updateSelectedArticlesStatus(GqlArticleReleaseStatus.Unreleased),
      },
    ];
  }

  private async deleteSelectedArticles() {
    await this.articleService.deleteArticles({
      where: {
        in: this.checkedEntities.map((a) => ({ id: a.id })),
      },
    });
  }

  private async updateSelectedArticlesStatus(status: GqlArticleReleaseStatus) {
    await this.articleService.updateArticles({
      where: {
        in: this.checkedEntities.map((a) => ({ id: a.id })),
      },
      data: {
        status: {
          set: status,
        },
      },
    });
  }
}
