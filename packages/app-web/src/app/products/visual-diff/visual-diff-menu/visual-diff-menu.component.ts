import { Component, OnInit } from '@angular/core';
import { RepositoryService } from '../../../services/repository.service';
import { AuthService } from '../../../services/auth.service';
import { Repository } from '../../../graphql/types';

@Component({
  selector: 'app-visual-diff-menu',
  templateUrl: './visual-diff-menu.component.html',
  styleUrls: ['./visual-diff-menu.component.scss'],
})
export class VisualDiffMenuComponent implements OnInit {
  repositories: Repository[] = [];

  constructor(
    private readonly repositoryService: RepositoryService,
    private readonly authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.authService.authorizationChange().subscribe((authenticated) => {
      if (authenticated) {
        this.fetchRepositories();
      }
    });
  }

  getPageUrl(repository: Repository): string {
    if (repository.sources.length > 0) {
      const url = repository.sources[0].page.url;
      return new URL(url).hostname;
    }
  }

  private async fetchRepositories() {
    const page = 0;
    this.repositories = await this.repositoryService.listRepositoriess({
      cursor: {
        page,
      },
    });
  }
}
