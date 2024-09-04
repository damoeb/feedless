import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
} from '@angular/core';
import { Repository, RepositoryFull, WebDocument } from '../../graphql/types';
import { RepositoryService } from '../../services/repository.service';
import { BubbleColor } from '../../components/bubble/bubble.component';
import { GqlProductCategory, GqlVisibility } from '../../../generated/graphql';
import { relativeTimeOrElse } from '../../components/agents/agents.component';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-feeds-page',
  templateUrl: './feeds.page.html',
  styleUrls: ['./feeds.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeedsPage implements OnInit {
  busy = false;
  currentPage: number = 0;
  documents: WebDocument[];
  repositories: RepositoryFull[] = [];
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
    await this.fetchFeeds(0);
  }

  protected async fetchFeeds(page: number) {
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
      'network-only',
    );
    this.isLastPage = repositories.length < pageSize;
    this.repositories = repositories;
    this.changeRef.detectChanges();
  }

  getHealthColorForFeed(repository: Repository): BubbleColor {
    return 'green';
  }

  isPrivate(repository: Repository): boolean {
    return repository.visibility === GqlVisibility.IsPrivate;
  }
}
