import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Record, Repository, RepositoryFull } from '../../graphql/types';
import { RepositoryService } from '../../services/repository.service';
import { BubbleColor } from '../../components/bubble/bubble.component';
import { GqlProductCategory, GqlVisibility } from '../../../generated/graphql';
import { relativeTimeOrElse } from '../../components/agents/agents.component';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { FetchPolicy } from '@apollo/client/core';
import { AppConfigService } from '../../services/app-config.service';
import dayjs from 'dayjs';

type ViewMode = 'list' | 'table';

@Component({
  selector: 'app-feeds-page',
  templateUrl: './feeds.page.html',
  styleUrls: ['./feeds.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeedsPage implements OnInit, OnDestroy {
  loading = false;
  currentPage: number = 0;
  documents: Record[];
  repositories: Repository[] = [];
  fromNow = relativeTimeOrElse;
  isLastPage: boolean;
  viewMode: ViewMode = 'list';
  viewModeTable: ViewMode = 'table';
  viewModeList: ViewMode = 'list';
  private subscriptions: Subscription[] = [];

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly activatedRoute: ActivatedRoute,
    private readonly appConfigService: AppConfigService,
    private readonly repositoryService: RepositoryService,
  ) {}

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
              eq: GqlProductCategory.Feedless,
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

  async downloadRepositories() {
    const rRepositories = await this.getAllRepositories()

    var a = window.document.createElement('a');
    a.href = window.URL.createObjectURL(new Blob([JSON.stringify(rRepositories, null, 2)], { type: 'application/json' }));
    a.download = `feedless-backup-${dayjs().format('YYYY-MM-DD')}.json`;

    document.body.appendChild(a);
    a.click();

    document.body.removeChild(a);
  }

  private async getAllRepositories(): Promise<RepositoryFull[]> {
    const repositories: RepositoryFull[] = [];

    let page = 0;
    while (true) {
      const repositoriesOnPage = await this.repositoryService.listRepositories(
        {
          cursor: {
            page,
          },
          where: {
            product: {
              eq: GqlProductCategory.Feedless,
            },
          },
        },
      );
      if (repositoriesOnPage.length === 0) {
        break;
      }
      for (let index = 0; index < repositoriesOnPage.length; index++) {
        repositories.push(await this.repositoryService.getRepositoryById(repositoriesOnPage[index].id));
      }
      page++;
    }
    return repositories;
  }

  uploadRepositories(uploadEvent: Event) {
    const file = (uploadEvent.target as any).files[0];
    const reader = new FileReader();
    reader.onload = async (e) => {
      const data: ArrayBuffer | string = (e.target as any).result;
      const repositories: RepositoryFull[] = JSON.parse(String(data));

      // TODO await this.repositoryService.createRepositories(repositories)

    };
    reader.readAsText(file);
  }
}
