import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Repository, WebDocument } from '../../graphql/types';
import { RepositoryService } from '../../services/repository.service';
import { BubbleColor } from '../../components/bubble/bubble.component';
import { GqlProductCategory, GqlVisibility } from '../../../generated/graphql';
import { relativeTimeOrElse } from '../../components/agents/agents.component';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { FetchPolicy } from '@apollo/client/core';

@Component({
  selector: 'app-feeds-page',
  templateUrl: './feeds.page.html',
  styleUrls: ['./feeds.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeedsPage implements OnInit, OnDestroy {
  busy = false;
  currentPage: number = 0;
  private subscriptions: Subscription[] = [];
  documents: WebDocument[];
  repositories: Repository[] = [];
  fromNow = relativeTimeOrElse;
  isLastPage: boolean;

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly activatedRoute: ActivatedRoute,
    private readonly titleService: Title,
    private readonly repositoryService: RepositoryService,
  ) {}

  async ngOnInit() {
    console.log('reload?', this.activatedRoute.snapshot.queryParams['reload']);
    this.titleService.setTitle('Feeds');

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

  protected async fetchFeeds(
    page: number,
    fetchPolicy: FetchPolicy = 'cache-first',
  ) {
    this.currentPage = page;
    const pageSize = 10;

    const repositories = await this.repositoryService.listRepositories(
      {
        cursor: {
          page,
          pageSize,
        },
        where: {
          product: {
            in: [GqlProductCategory.RssProxy],
          },
        },
      },
      fetchPolicy,
    );
    this.isLastPage = repositories.length < pageSize;
    this.repositories = repositories;
    this.changeRef.detectChanges();
  }

  getHealthColorForFeed(repository: Repository): BubbleColor {
    if (repository.hasDisabledSources) {
      return 'red';
    } else {
      return 'green';
    }
  }

  isPrivate(repository: Repository): boolean {
    return repository.visibility === GqlVisibility.IsPrivate;
  }
}
