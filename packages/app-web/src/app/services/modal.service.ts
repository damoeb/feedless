import { Injectable } from '@angular/core';
import {
  CodeEditorModalComponent,
  CodeEditorModalComponentProps,
} from '../modals/code-editor-modal/code-editor-modal.component';
import { ModalController } from '@ionic/angular';
import { Router } from '@angular/router';
import {
  FeedBuilderModalComponent,
  FeedBuilderModalComponentProps,
} from '../modals/feed-builder-modal/feed-builder-modal.component';
import {
  FeedBuilder,
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
import { DeepPartial } from '../types';
import { RemoteFeedModalComponent, RemoteFeedModalComponentProps } from '../modals/remote-feed-modal/remote-feed-modal.component';

@Injectable({
  providedIn: 'root',
})
export class ModalService {
  private readonly unfinishedWizardKey = 'unfinished-wizard';

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly router: Router,
  ) {}

  async openCodeEditorModal(code: string = null): Promise<FeedBuilder | null> {
    const componentProps: CodeEditorModalComponentProps = {
      code: code ?? '',
    };
    const modal = await this.modalCtrl.create({
      component: CodeEditorModalComponent,
      componentProps,
    });

    await modal.present();
    const response = await modal.onDidDismiss<string | null>();
    if (response.data) {
      return JSON.parse(response.data);
    } else {
      return null;
    }
  }

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
    const { data, role } = await modal.onDidDismiss<string[]>();
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
