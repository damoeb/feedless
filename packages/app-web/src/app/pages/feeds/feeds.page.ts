import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Record, RepositoryWithFrequency } from '../../graphql/types';
import { RepositoryService } from '../../services/repository.service';
import {
  BubbleColor,
  BubbleComponent,
} from '../../components/bubble/bubble.component';
import { GqlVertical, GqlVisibility } from '../../../generated/graphql';
import { relativeTimeOrElse } from '../../components/agents/agents.component';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { FetchPolicy } from '@apollo/client/core';
import { AppConfigService } from '../../services/app-config.service';
import {
  IonBreadcrumb,
  IonBreadcrumbs,
  IonButtons,
  IonChip,
  IonCol,
  IonContent,
  IonItem,
  IonLabel,
  IonList,
  IonProgressBar,
  IonRow,
} from '@ionic/angular/standalone';
import { NgClass } from '@angular/common';
import { ImportButtonComponent } from '../../components/import-button/import-button.component';
import { TableComponent } from '../../components/table/table.component';
import { HistogramComponent } from '../../components/histogram/histogram.component';
import { PaginationComponent } from '../../components/pagination/pagination.component';

type ViewMode = 'list' | 'table';

@Component({
  selector: 'app-feeds-page',
  templateUrl: './feeds.page.html',
  styleUrls: ['./feeds.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IonContent,
    NgClass,
    IonBreadcrumbs,
    IonBreadcrumb,
    RouterLink,
    IonRow,
    IonCol,
    IonButtons,
    ImportButtonComponent,
    IonProgressBar,
    TableComponent,
    IonList,
    IonItem,
    BubbleComponent,
    IonLabel,
    IonChip,
    HistogramComponent,
    PaginationComponent,
  ],
  standalone: true,
})
export class FeedsPage implements OnInit, OnDestroy {
  private readonly changeRef = inject(ChangeDetectorRef);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly appConfigService = inject(AppConfigService);
  private readonly repositoryService = inject(RepositoryService);

  loading = false;
  currentPage: number = 0;
  documents: Record[];
  repositories: RepositoryWithFrequency[] = [];
  fromNow = relativeTimeOrElse;
  isLastPage: boolean;
  viewMode: ViewMode = 'list';
  viewModeTable: ViewMode = 'table';
  viewModeList: ViewMode = 'list';
  private subscriptions: Subscription[] = [];

  async ngOnInit() {
    this.appConfigService.setPageTitle('Feeds');

    this.subscriptions.push(
      this.activatedRoute.queryParams.subscribe(async (queryParams) => {
        if (queryParams.reload) {
          await this.fetchFeeds(0, 'network-only');
        }
      }),
    );

    await this.fetchFeeds(0);
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  getHealthColorForFeed(repository: RepositoryWithFrequency): BubbleColor {
    if (repository.sourcesCount === repository.sourcesCountWithProblems) {
      return 'red';
    } else {
      if (repository.sourcesCountWithProblems > 0) {
        return 'orange';
      } else {
        return 'green';
      }
    }
  }

  isPrivate(repository: RepositoryWithFrequency): boolean {
    return repository.visibility === GqlVisibility.IsPrivate;
  }

  protected async fetchFeeds(
    page: number,
    fetchPolicy: FetchPolicy = 'cache-first',
  ) {
    this.loading = true;
    this.currentPage = page;
    this.changeRef.detectChanges();
    const pageSize = 10;

    try {
      const repositories = await this.repositoryService.listRepositories(
        {
          cursor: {
            page,
            pageSize,
          },
          where: {
            product: {
              eq: GqlVertical.Feedless,
            },
          },
        },
        fetchPolicy,
      );
      this.isLastPage = repositories.length == 0;
      this.repositories = repositories;
    } finally {
      this.loading = false;
    }
    this.changeRef.detectChanges();
  }
}
