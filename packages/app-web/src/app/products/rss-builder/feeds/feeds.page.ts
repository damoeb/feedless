import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
} from '@angular/core';
import { Repository, WebDocument } from '../../../graphql/types';
import { RepositoryService } from '../../../services/repository.service';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import { BubbleColor } from '../../../components/bubble/bubble.component';
import { GqlVisibility } from '../../../../generated/graphql';

@Component({
  selector: 'app-feeds-page',
  templateUrl: './feeds.page.html',
  styleUrls: ['./feeds.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeedsPage implements OnInit {
  busy = false;
  documents: WebDocument[];
  repositories: Repository[] = [];

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly repositoryService: RepositoryService,
  ) {}

  async ngOnInit() {
    dayjs.extend(relativeTime);
    await this.fetchFeeds();
  }

  private async fetchFeeds() {
    const page = 0;
    const repositories = await this.repositoryService.listRepositories({
      cursor: {
        page,
      },
    });
    this.repositories.push(...repositories);
    this.changeRef.detectChanges();
  }

  getHealthColorForFeed(repository: Repository): BubbleColor {
    if (repository.sources.some((source) => source.errornous)) {
      return 'red';
    } else {
      return 'blue';
    }
  }

  isPrivate(repository: Repository): boolean {
    return repository.visibility === GqlVisibility.IsPrivate;
  }

  fromNow(date: number) {
    return dayjs(date).toNow(true);
  }
}
