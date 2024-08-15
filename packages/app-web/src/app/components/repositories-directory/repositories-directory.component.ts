import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
} from '@angular/core';
import { ServerConfigService } from '../../services/server-config.service';
import { RepositoryService } from '../../services/repository.service';
import { Repository } from '../../graphql/types';

@Component({
  selector: 'app-repositories-directory',
  templateUrl: './repositories-directory.component.html',
  styleUrls: ['./repositories-directory.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RepositoriesDirectoryComponent implements OnInit {
  repositories: Repository[] = [];

  constructor(
    private readonly repositoryService: RepositoryService,
    private readonly changeRef: ChangeDetectorRef,
    readonly serverConfig: ServerConfigService,
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
