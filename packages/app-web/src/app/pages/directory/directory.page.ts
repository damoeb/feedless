import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
} from '@angular/core';
import { PublicRepository, Repository, Record } from '../../graphql/types';
import { RepositoryService } from '../../services/repository.service';
import { BubbleColor } from '../../components/bubble/bubble.component';
import { GqlProductCategory, GqlVisibility } from '../../../generated/graphql';
import { relativeTimeOrElse } from '../../components/agents/agents.component';
import { Title } from '@angular/platform-browser';
import { FormControl } from '@angular/forms';

type ViewMode = 'list' | 'grid';

@Component({
  selector: 'app-feeds-page',
  templateUrl: './directory.page.html',
  styleUrls: ['./directory.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DirectoryPage implements OnInit {
  loading = false;
  currentPage: number = 0;
  documents: Record[];
  repositories: PublicRepository[] = [];
  fromNow = relativeTimeOrElse;
  isLastPage: boolean;
  viewModeList: ViewMode = 'list';
  viewModeGrid: ViewMode = 'grid';
  viewModeFc = new FormControl<ViewMode>('list');

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly titleService: Title,
    private readonly repositoryService: RepositoryService,
  ) {}

  async ngOnInit() {
    this.titleService.setTitle('Directory');
    await this.fetchFeeds(0);
  }

  protected async fetchFeeds(page: number) {
    this.currentPage = page;
    const pageSize = 10;
    this.loading = true;
    this.changeRef.detectChanges();

    try {
      const repositories = await this.repositoryService.listPublicRepositories({
        cursor: {
          page,
          pageSize,
        },
        // orderBy: {
        //
        // },
        where: {
          visibility: { in: [GqlVisibility.IsPublic] },
          // product: {
          //   in: [GqlProductCategory.RssProxy],
          // },
        },
      });
      this.isLastPage = repositories.length < pageSize;
      this.repositories = repositories;
    } finally {
      this.loading = false;
    }
    this.changeRef.detectChanges();
  }

  getHealthColorForFeed(repository: PublicRepository): BubbleColor {
    return repository.archived ? 'gray' : 'green';
  }

  isPrivate(repository: PublicRepository): boolean {
    return repository.visibility === GqlVisibility.IsPrivate;
  }
}
