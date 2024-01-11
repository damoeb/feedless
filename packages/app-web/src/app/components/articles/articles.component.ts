import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit } from '@angular/core';
import { ArticleService } from '../../services/article.service';
import { ActionSheetButton } from '@ionic/core/dist/types/components/action-sheet/action-sheet-interface';
import { GqlArticleReleaseStatus } from '../../../generated/graphql';
import { FetchPolicy } from '@apollo/client/core';
import { WebDocumentService } from '../../services/web-document.service';
import { WebDocument } from '../../graphql/types';

@Component({
  selector: 'app-articles',
  templateUrl: './articles.component.html',
  styleUrls: ['./articles.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ArticlesComponent
  implements OnInit
{
  @Input()
  streamId: string;
  @Input()
  name: string;
  @Input()
  isOwner: boolean;

  entities: WebDocument[];
  isLast: boolean = false;

  constructor(
    private readonly articleService: ArticleService,
    private readonly webDocumentService: WebDocumentService,
    private readonly changeRef: ChangeDetectorRef,
  ) {
  }

  async ngOnInit() {
    this.entities = await this.fetch(0, 'network-only');
    this.changeRef.detectChanges();
  }

  async fetch(
    page: number = 0,
    fetchPolicy: FetchPolicy,
  ) {
    const entities = await this.webDocumentService.findAllByStreamId({
      cursor: {
        page
      },
      where: {
        sourceSubscription: {
          where: {
            id: this.streamId
          }
        }
      }
    }, fetchPolicy);

    this.isLast = entities.length < 10;
    return entities;
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
    // await this.articleService.deleteArticles({
    //   where: {
    //     in: this.checkedEntities.map((a) => ({ id: a.id })),
    //   },
    // });
  }

  private async updateSelectedArticlesStatus(status: GqlArticleReleaseStatus) {
    // await this.articleService.updateArticles({
    //   where: {
    //     in: this.checkedEntities.map((a) => ({ id: a.id })),
    //   },
    //   data: {
    //     status: {
    //       set: status,
    //     },
    //   },
    // });
  }

}
