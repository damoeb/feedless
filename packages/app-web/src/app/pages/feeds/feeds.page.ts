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

@Component({
  selector: 'app-feeds-page',
  templateUrl: './feeds.page.html',
  styleUrls: ['./feeds.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeedsPage implements OnInit {
  busy = false;
  documents: WebDocument[];
  repositories: RepositoryFull[] = [];

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly titleService: Title,
    private readonly repositoryService: RepositoryService,
  ) {}

  async ngOnInit() {
    this.titleService.setTitle('Feeds');
    await this.fetchFeeds();
  }

  private async fetchFeeds() {
    const page = 0;

    const repositories = await this.repositoryService.listRepositories({
      cursor: {
        page,
      },
      where: {
        product: {
          in: [GqlProductCategory.RssProxy],
        },
      },
    }, 'network-only');
    this.repositories.push(...repositories);
    this.changeRef.detectChanges();
  }

  getHealthColorForFeed(repository: Repository): BubbleColor {
    return 'green';
  }

  isPrivate(repository: Repository): boolean {
    return repository.visibility === GqlVisibility.IsPrivate;
  }

  fromNow = relativeTimeOrElse;
}
