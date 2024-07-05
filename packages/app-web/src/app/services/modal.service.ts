import { Injectable } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { Router } from '@angular/router';
import {
  FeedBuilderModalComponent,
  FeedBuilderModalComponentProps,
} from '../modals/feed-builder-modal/feed-builder-modal.component';
import {
  FeedBuilderModalComponentExitRole,
  FeedWithRequest,
} from '../components/feed-builder/feed-builder.component';
import {
  GenerateFeedModalComponent,
  GenerateFeedModalComponentProps,
} from '../modals/generate-feed-modal/generate-feed-modal.component';
import {
  TrackerEditModalComponent,
  TrackerEditModalComponentProps,
} from '../products/pc-tracker/tracker-edit/tracker-edit-modal.component';
import {
  TagsModalComponent,
  TagsModalComponentProps,
} from '../modals/tags-modal/tags-modal.component';
import {
  RemoteFeedModalComponent,
  RemoteFeedModalComponentProps,
} from '../modals/remote-feed-modal/remote-feed-modal.component';
import {
  SearchAddressModalComponent,
} from '../modals/search-address-modal/search-address-modal.component';
import { OsmMatch } from './open-street-map.service';

@Injectable({
  providedIn: 'root',
})
export class ModalService {
  private readonly unfinishedWizardKey = 'unfinished-wizard';

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly router: Router,
  ) {}

  async openFeedBuilder(
    componentProps: FeedBuilderModalComponentProps,
    overwriteHandler: (
      data: FeedWithRequest,
      role: String,
    ) => Promise<void> = null,
  ) {
    const modal = await this.modalCtrl.create({
      component: FeedBuilderModalComponent,
      componentProps,
      cssClass: 'fullscreen-modal',
      showBackdrop: true,
      backdropDismiss: false,
    });
    await modal.present();
    const { data, role } = await modal.onDidDismiss<FeedWithRequest>();

    if (overwriteHandler) {
      await overwriteHandler(data, role);
    } else {
      switch (role) {
        case FeedBuilderModalComponentExitRole.login:
          localStorage.setItem(this.unfinishedWizardKey, JSON.stringify(data));
          await this.router.navigateByUrl('/login');
          break;
        case FeedBuilderModalComponentExitRole.dismiss:
          break;
      }
    }
  }

  async openTagModal(
    componentProps: TagsModalComponentProps,
  ): Promise<string[]> {
    const modal = await this.modalCtrl.create({
      component: TagsModalComponent,
      componentProps,
      cssClass: 'tiny-modal',
      showBackdrop: true,
      backdropDismiss: false,
    });
    await modal.present();
    const { data } = await modal.onDidDismiss<string[]>();
    return data;
  }

  async openRemoteFeedModal(
    componentProps: RemoteFeedModalComponentProps,
  ): Promise<void> {
    const modal = await this.modalCtrl.create({
      component: RemoteFeedModalComponent,
      componentProps,
      // cssClass: 'fullscreen-modal',
      showBackdrop: true,
      backdropDismiss: true,
    });
    await modal.present();
  }

  async openSearchAddressModal(): Promise<OsmMatch> {
    const modal = await this.modalCtrl.create({
      component: SearchAddressModalComponent,
      // cssClass: 'fullscreen-modal',
      showBackdrop: true,
      backdropDismiss: true,
    });
    await modal.present();

    const response = await modal.onDidDismiss<OsmMatch>();
    return response.data;
  }

  async openFeedMetaEditor(componentProps: GenerateFeedModalComponentProps) {
    const modal = await this.modalCtrl.create({
      component: GenerateFeedModalComponent,
      cssClass: 'fullscreen-modal',
      componentProps,
    });

    await modal.present();
  }

  async openPageTrackerEditor(componentProps: TrackerEditModalComponentProps) {
    const modal = await this.modalCtrl.create({
      component: TrackerEditModalComponent,
      cssClass: 'fullscreen-modal',
      componentProps,
    });

    await modal.present();
  }
}
