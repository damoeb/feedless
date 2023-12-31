import { Injectable } from '@angular/core';
import {
  CodeEditorModalComponent,
  CodeEditorModalComponentProps,
} from '../modals/code-editor-modal/code-editor-modal.component';
import { ModalController } from '@ionic/angular';
import {
  FeedBuilder,
  FeedBuilderModalComponent,
  FeedBuilderModalComponentProps,
} from '../modals/feed-builder-modal/feed-builder-modal.component';
import {
  AgentsModalComponent,
  AgentsModalComponentProps,
} from '../modals/agents-modal/agents-modal.component';
import { Agent } from './agent.service';
import { BasicBucket } from '../graphql/types';
import { BucketCreateModalComponent } from '../modals/bucket-create-modal/bucket-create-modal.component';

@Injectable({
  providedIn: 'root',
})
export class ModalService {
  constructor(private readonly modalCtrl: ModalController) {}

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

  async openFeedBuilder(componentProps: FeedBuilderModalComponentProps) {
    const modal = await this.modalCtrl.create({
      component: FeedBuilderModalComponent,
      componentProps,
      cssClass: 'modal-dialog',
      showBackdrop: true,
      backdropDismiss: false,
    });
    await modal.present();
  }

  async openAgentModal(
    componentProps: AgentsModalComponentProps,
  ): Promise<Agent | null> {
    const modal = await this.modalCtrl.create({
      component: AgentsModalComponent,
      componentProps,
      cssClass: 'modal-dialog',
      showBackdrop: true,
      backdropDismiss: false,
    });
    await modal.present();

    const response = await modal.onDidDismiss<Agent | null>();
    if (response.data) {
      return response.data;
    } else {
      return null;
    }
  }

  async openCreateBucketModal(): Promise<BasicBucket | null> {
    const modal = await this.modalCtrl.create({
      component: BucketCreateModalComponent,
      cssClass: 'modal-dialog',
      showBackdrop: true,
      backdropDismiss: false,
    });
    await modal.present();

    const response = await modal.onDidDismiss<BasicBucket | null>();
    console.log('openCreateBucketModal', response);
    if (response.data) {
      return response.data;
    } else {
      return null;
    }
  }
}
