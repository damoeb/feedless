import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ServerSettingsService } from '../../../services/server-settings.service';
import { RepositoryService } from '../../../services/repository.service';
import { Repository } from '../../../graphql/types';

@Component({
  selector: 'app-feed-tiles-page',
  templateUrl: './feed-tiles.page.html',
  styleUrls: ['./feed-tiles.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeedTilesPage implements OnInit {
  repositories: Repository[] = [];

  constructor(
    private readonly repositoryService: RepositoryService,
    private readonly changeRef: ChangeDetectorRef,
    readonly serverSettings: ServerSettingsService,
  ) {}

  async handleQuery(query: string) {
    try {
      // await this.router.navigate(['/draft'], {
      //   queryParams: {
      //     code,
      //   },
      // });
    } catch (e) {
      console.warn(e);
    }
  }

  async ngOnInit() {
    await this.fetch();
  }
  async fetch() {
    const repositories = await this.repositoryService.listRepositories({
      cursor: {
        page: 0,
        pageSize: 30,
      },
    });
    this.repositories.push(...repositories);
    this.changeRef.detectChanges();
  }
}
