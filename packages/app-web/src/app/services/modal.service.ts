import { Injectable } from '@angular/core';
import { ModalController } from '@ionic/angular/standalone';
import { ActivatedRoute, Router } from '@angular/router';
import {
  FeedBuilderModalComponent,
  FeedBuilderModalComponentProps,
} from '../modals/feed-builder-modal/feed-builder-modal.component';
import {
  FeedBuilderModalComponentExitRole,
  FeedOrRepository,
} from '../components/feed-builder/feed-builder.component';
import {
  RepositoryModalComponent,
  RepositoryModalComponentProps,
} from '../modals/repository-modal/repository-modal.component';
import {
  TrackerEditModalComponent,
  TrackerEditModalComponentProps,
} from '../products/change-tracker/tracker-edit/tracker-edit-modal.component';
import {
  TagsModalComponent,
  TagsModalComponentProps,
} from '../modals/tags-modal/tags-modal.component';
import { SearchAddressModalComponent } from '../modals/search-address-modal/search-address-modal.component';
import { OsmMatch } from './open-street-map.service';
import {
  CodeEditorModalComponent,
  CodeEditorModalComponentProps,
} from '../modals/code-editor-modal/code-editor-modal.component';
import {
  MapModalComponent,
  MapModalComponentProps,
} from '../modals/map-modal/map-modal.component';
import { LatLon } from '../components/map/map.component';
import { Nullable } from '../types';

export enum ModalName {
  editRepository = 'EditRepository',
  searchAddress = 'SearchAddress',
  codeEditor = 'CodeEditor',
  tagEditor = 'TagEditor',
  feedBuilder = 'FeedBuilder',
  editTracker = 'EditTracker',
}

@Injectable({
  providedIn: 'root',
})
export class ModalService {
  private readonly unfinishedWizardKey = 'unfinished-wizard';

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly activatedRoute: ActivatedRoute,
    private readonly router: Router,
  ) {}

  async openFeedBuilder(
    componentProps: FeedBuilderModalComponentProps,
    overwriteHandler: Nullable<
      (
        data: Nullable<FeedOrRepository>,
        role: Nullable<String>,
      ) => Promise<void>
    > = null,
  ) {
    await this.updateUrlParams(ModalName.feedBuilder);
    const modal = await this.modalCtrl.create({
      component: FeedBuilderModalComponent,
      componentProps,
      cssClass: 'fullscreen-modal',
      showBackdrop: true,
      backdropDismiss: false,
    });
    await modal.present();
    const { data, role } = await modal.onDidDismiss<FeedOrRepository>();
    await this.updateUrlParams();

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
    await this.updateUrlParams(ModalName.tagEditor);
    const modal = await this.modalCtrl.create({
      component: TagsModalComponent,
      componentProps,
      cssClass: 'tiny-modal',
      showBackdrop: true,
      backdropDismiss: false,
    });
    await modal.present();
    const { data } = await modal.onDidDismiss<string[]>();
    await this.updateUrlParams();
    return data!;
  }

  async openMapModal(componentProps: MapModalComponentProps): Promise<LatLon> {
    await this.updateUrlParams(ModalName.tagEditor);
    const modal = await this.modalCtrl.create({
      component: MapModalComponent,
      componentProps,
      showBackdrop: true,
      backdropDismiss: false,
    });
    await modal.present();
    const { data } = await modal.onDidDismiss<LatLon>();
    await this.updateUrlParams();
    return data!;
  }

  async openCodeEditorModal(
    componentProps: CodeEditorModalComponentProps,
  ): Promise<string[]> {
    await this.updateUrlParams(ModalName.codeEditor);
    const modal = await this.modalCtrl.create({
      component: CodeEditorModalComponent,
      componentProps,
      showBackdrop: true,
      backdropDismiss: false,
    });
    await modal.present();
    const { data } = await modal.onDidDismiss();
    await this.updateUrlParams();
    return data;
  }

  // async openRemoteFeedModal(
  //   componentProps: RemoteFeedModalComponentProps,
  // ): Promise<void> {
  //   const modal = await this.modalCtrl.create({
  //     component: RemoteFeedModalComponent,
  //     componentProps,
  //     // cssClass: 'fullscreen-modal',
  //     showBackdrop: true,
  //     backdropDismiss: true,
  //   });
  //   await modal.present();
  // }

  async openSearchAddressModal(): Promise<OsmMatch> {
    await this.updateUrlParams(ModalName.searchAddress);
    const modal = await this.modalCtrl.create({
      component: SearchAddressModalComponent,
      // cssClass: 'fullscreen-modal',
      showBackdrop: true,
      backdropDismiss: true,
    });
    await modal.present();

    const response = await modal.onDidDismiss<OsmMatch>();
    await this.updateUrlParams();
    return response.data!;
  }

  async openRepositoryEditor(componentProps: RepositoryModalComponentProps) {
    await this.updateUrlParams(ModalName.editRepository);
    const modal = await this.modalCtrl.create({
      component: RepositoryModalComponent,
      componentProps,
    });

    await modal.present();
    await modal.onDidDismiss();
    await this.updateUrlParams();
  }

  async openPageTrackerEditor(componentProps: TrackerEditModalComponentProps) {
    await this.updateUrlParams(ModalName.editTracker);
    const modal = await this.modalCtrl.create({
      component: TrackerEditModalComponent,
      cssClass: 'fullscreen-modal',
      componentProps,
    });

    await modal.present();
    await modal.onDidDismiss();
    await this.updateUrlParams();
  }

  private async updateUrlParams(modal: ModalName | undefined = undefined) {
    await this.router.navigate(this.activatedRoute.snapshot.url, {
      queryParams: modal
        ? {
            modal,
          }
        : {},
      relativeTo: this.activatedRoute,
      queryParamsHandling: modal ? 'merge' : undefined,
      replaceUrl: true,
    });
  }
}
