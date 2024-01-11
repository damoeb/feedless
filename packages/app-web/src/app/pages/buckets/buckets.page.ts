import { Component, OnInit } from '@angular/core';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import { ModalController } from '@ionic/angular';
import { GqlContentCategoryTag, GqlVisibility } from '../../../generated/graphql';
import { FilterData } from '../../components/filter-toolbar/filter-toolbar.component';
import { ProfileService } from 'src/app/services/profile.service';
import { AuthService } from 'src/app/services/auth.service';
import { OpmlService } from '../../services/opml.service';
import { ImportModalComponent } from '../../modals/import-modal/import-modal.component';
import { visibilityToLabel } from './bucket/bucket.page';
import { BasicBucket, SourceSubscription } from '../../graphql/types';
import { ActivatedRoute } from '@angular/router';
import { ExportModalComponent } from '../../modals/export-modal/export-modal.component';
import { SourceSubscriptionService } from '../../services/source-subscription.service';

@Component({
  selector: 'app-buckets-page',
  templateUrl: './buckets.page.html',
  styleUrls: ['./buckets.page.scss'],
})
export class BucketsPage implements OnInit {

  gridLayout = false;
  entities: SourceSubscription[] =
    [];
  isLast = false;
  private currentPage = 0;

  constructor(
    private readonly apollo: ApolloClient<any>,
    private readonly sourceSubscriptionService: SourceSubscriptionService,
    private readonly modalCtrl: ModalController,
    private readonly profileService: ProfileService,
    private readonly opmlService: OpmlService,
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

  async handleImport(): Promise<void> {
    const modal = await this.modalCtrl.create({
      component: ImportModalComponent,
      showBackdrop: true,
    });
    await modal.present();
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

  isOwner(entity: BasicBucket): boolean {
    return this.profileService.getUserId() === entity.ownerId;
  }

  async nextPage(event: any) {
    console.log('nextPage');
    this.currentPage += 1;
    await this.refetch();
    await event.target.complete();
  }

  async firstPage(
    filterData: FilterData<{
      tag: GqlContentCategoryTag;
      visibility: GqlVisibility;
    }>,
  ) {
    await this.fetch(this.currentPage);
  }

  private async fetch(
    page: number,
    fetchPolicy: FetchPolicy = 'cache-first',
  ): Promise<void> {
    const entities = await this.sourceSubscriptionService.listSourceSubscriptions(
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
