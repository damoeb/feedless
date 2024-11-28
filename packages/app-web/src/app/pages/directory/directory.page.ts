import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
} from '@angular/core';
import { PublicRepository, Record } from '../../graphql/types';
import { RepositoryService } from '../../services/repository.service';
import {
  BubbleColor,
  BubbleComponent,
} from '../../components/bubble/bubble.component';
import { GqlVisibility } from '../../../generated/graphql';
import { relativeTimeOrElse } from '../../components/agents/agents.component';
import { FormControl } from '@angular/forms';
import { AppConfigService } from '../../services/app-config.service';
import { addIcons } from 'ionicons';
import { trendingDownOutline } from 'ionicons/icons';
import { FeedlessHeaderComponent } from '../../components/feedless-header/feedless-header.component';
import {
  IonHeader,
  IonProgressBar,
  IonContent,
  IonRow,
  IonItem,
  IonList,
  IonLabel,
  IonChip,
  IonButton,
  IonIcon,
} from '@ionic/angular/standalone';
import { NgIf, NgFor } from '@angular/common';
import { ProductHeaderComponent } from '../../components/product-header/product-header.component';
import { RouterLink } from '@angular/router';
import { RemoveIfProdDirective } from '../../directives/remove-if-prod/remove-if-prod.directive';
import { PaginationComponent } from '../../components/pagination/pagination.component';

type ViewMode = 'list' | 'grid';

@Component({
  selector: 'app-feeds-page',
  templateUrl: './directory.page.html',
  styleUrls: ['./directory.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FeedlessHeaderComponent,
    IonHeader,
    NgIf,
    IonProgressBar,
    IonContent,
    ProductHeaderComponent,
    IonRow,
    IonItem,
    IonList,
    NgFor,
    BubbleComponent,
    IonLabel,
    RouterLink,
    IonChip,
    RemoveIfProdDirective,
    IonButton,
    IonIcon,
    PaginationComponent,
  ],
  standalone: true,
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
    private readonly appConfig: AppConfigService,
    private readonly repositoryService: RepositoryService,
  ) {
    addIcons({ trendingDownOutline });
  }

  async ngOnInit() {
    this.appConfig.setPageTitle('Directory');
    await this.fetchFeeds(0);
  }

  getHealthColorForFeed(repository: PublicRepository): BubbleColor {
    return repository.archived ? 'gray' : 'green';
  }

  isPrivate(repository: PublicRepository): boolean {
    return repository.visibility === GqlVisibility.IsPrivate;
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
}
