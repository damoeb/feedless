import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
} from '@angular/core';
import { FetchPolicy } from '@apollo/client/core';
import { ModalController } from '@ionic/angular';
import { Repository } from '../../graphql/types';
import { ActivatedRoute } from '@angular/router';
import { ExportModalComponent } from '../../modals/export-modal/export-modal.component';
import { RepositoryService } from '../../services/repository.service';
import { GqlVisibility } from '../../../generated/graphql';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-sources-page',
  templateUrl: './repositories.page.html',
  styleUrls: ['./repositories.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RepositoriesPage implements OnInit {
  gridLayout = false;
  repositories: Repository[] = [];
  isLast = false;
  private currentPage = 0;

  constructor(
    private readonly repositoryService: RepositoryService,
    private readonly modalCtrl: ModalController,
    private readonly titleService: Title,
    private readonly changeRef: ChangeDetectorRef,
    private readonly activatedRoute: ActivatedRoute,
  ) {}

  ngOnInit() {
    this.titleService.setTitle('Repositories');
    this.activatedRoute.url.subscribe(async () => {
      this.repositories = [];
      this.currentPage = 0;
      await this.refetch('network-only');
    });
  }

  async handleExport() {
    const modal = await this.modalCtrl.create({
      component: ExportModalComponent,
      showBackdrop: true,
    });
    await modal.present();
  }

  async nextPage(event: any) {
    console.log('nextPage');
    this.currentPage += 1;
    await this.refetch();
    await event.target.complete();
  }

  private async fetch(
    page: number,
    fetchPolicy: FetchPolicy = 'cache-first',
  ): Promise<void> {
    const entities = await this.repositoryService.listRepositories(
      {
        cursor: {
          page,
        },
      },
      fetchPolicy,
    );

    this.isLast = entities.length < 10;
    this.repositories.push(...entities);
    this.changeRef.detectChanges();
  }

  private async refetch(fetchPolicy: FetchPolicy = 'cache-first') {
    console.log('refetch');
    await this.fetch(this.currentPage, fetchPolicy);
  }

  toLabel(visibility: GqlVisibility) {
    switch (visibility) {
      case GqlVisibility.IsPrivate:
        return 'Private';
      case GqlVisibility.IsPublic:
        return 'Public';
    }
  }
}
