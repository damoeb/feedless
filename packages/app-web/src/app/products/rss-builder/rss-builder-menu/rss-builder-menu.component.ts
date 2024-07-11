import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
} from '@angular/core';
import { RepositoryService } from '../../../services/repository.service';
import { AuthService } from '../../../services/auth.service';
import { Repository } from '../../../graphql/types';
import { filter } from 'rxjs';
import { getFirstFetchUrlLiteral } from '../../../utils';

@Component({
  selector: 'app-rss-builder-menu',
  templateUrl: './rss-builder-menu.component.html',
  styleUrls: ['./rss-builder-menu.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RssBuilderMenuComponent implements OnInit {
  repositories: Repository[] = [];

  constructor(
    private readonly repositoryService: RepositoryService,
    private readonly authService: AuthService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.authService
      .authorizationChange()
      .pipe(filter((authenticated) => authenticated?.loggedIn))
      .subscribe((authenticated) => {
        if (authenticated.loggedIn) {
          this.fetchFeeds();
        }
      });
  }

  getPageUrl(repository: Repository): string {
    if (repository.sources.length > 0) {
      const url = getFirstFetchUrlLiteral(repository.sources[0].flow.sequence);
      return new URL(url).hostname;
    }
  }

  private async fetchFeeds() {
    const page = 0;
    this.repositories = await this.repositoryService.listRepositories({
      cursor: {
        page,
      },
    });
    this.changeRef.detectChanges();
  }
}
