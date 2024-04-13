import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
} from '@angular/core';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import { ModalController } from '@ionic/angular';
import { SessionService } from 'src/app/services/session.service';
import { AuthService } from 'src/app/services/auth.service';
import { SourceSubscription } from '../../graphql/types';
import { ActivatedRoute } from '@angular/router';
import { ExportModalComponent } from '../../modals/export-modal/export-modal.component';
import { SourceSubscriptionService } from '../../services/source-subscription.service';
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
  entities: SourceSubscription[] = [];
  isLast = false;
  private currentPage = 0;

  constructor(
    private readonly apollo: ApolloClient<any>,
    private readonly sourceSubscriptionService: SourceSubscriptionService,
    private readonly modalCtrl: ModalController,
    private readonly profileService: SessionService,
    private readonly authService: AuthService,
    private readonly titleService: Title,
    private readonly changeRef: ChangeDetectorRef,
    private readonly activatedRoute: ActivatedRoute,
  ) {}

  ngOnInit() {
    this.titleService.setTitle('Repositories');
    this.activatedRoute.url.subscribe(async () => {
      this.entities = [];
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
    const entities =
      await this.sourceSubscriptionService.listSourceSubscriptions(
        {
          cursor: {
            page,
          },
        },
        fetchPolicy,
      );

    this.isLast = entities.length < 10;
    this.entities.push(...entities);
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
