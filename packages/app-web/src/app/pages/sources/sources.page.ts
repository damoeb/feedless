import { Component, OnInit } from '@angular/core';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import { ModalController } from '@ionic/angular';
import { GqlVisibility } from '../../../generated/graphql';
import { ProfileService } from 'src/app/services/profile.service';
import { AuthService } from 'src/app/services/auth.service';
import { visibilityToLabel } from './source/source.page';
import { SourceSubscription } from '../../graphql/types';
import { ActivatedRoute } from '@angular/router';
import { ExportModalComponent } from '../../modals/export-modal/export-modal.component';
import { SourceSubscriptionService } from '../../services/source-subscription.service';

@Component({
  selector: 'app-sources-page',
  templateUrl: './sources.page.html',
  styleUrls: ['./sources.page.scss'],
})
export class SourcesPage implements OnInit {
  gridLayout = false;
  entities: SourceSubscription[] = [];
  isLast = false;
  private currentPage = 0;

  constructor(
    private readonly apollo: ApolloClient<any>,
    private readonly sourceSubscriptionService: SourceSubscriptionService,
    private readonly modalCtrl: ModalController,
    private readonly profileService: ProfileService,
    private readonly authService: AuthService,
    private readonly activatedRoute: ActivatedRoute,
  ) {}

  ngOnInit() {
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
  label(visibility: GqlVisibility): string {
    return visibilityToLabel(visibility);
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
  }

  private async refetch(fetchPolicy: FetchPolicy = 'cache-first') {
    console.log('refetch');
    await this.fetch(this.currentPage, fetchPolicy);
  }
}
