import { Injectable } from '@angular/core';
import {
  CodeEditorModalComponent,
  CodeEditorModalComponentProps,
} from '../modals/code-editor-modal/code-editor-modal.component';
import { ModalController } from '@ionic/angular';
import {
  DeepPartial,
  FeedBuilder,
  FeedBuilderModalComponent,
  FeedBuilderModalComponentProps, FeedBuilderModalData
} from '../modals/feed-builder-modal/feed-builder-modal.component';
import {
  AgentsModalComponent,
  AgentsModalComponentProps,
} from '../modals/agents-modal/agents-modal.component';
import { Agent } from './agent.service';
import { BasicBucket } from '../graphql/types';
import { BucketCreateModalComponent } from '../modals/bucket-create-modal/bucket-create-modal.component';
import { WizardExitRole } from '../components/wizard/wizard/wizard.component';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class ModalService {
  private readonly unfinishedWizardKey = 'unfinished-wizard';

  constructor(private readonly modalCtrl: ModalController,
              private readonly router: Router) {}

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
    const {data, role} = await modal.onDidDismiss<FeedBuilderModalData>();

    const saveWizardContext = (data: FeedBuilderModalData) => {
      localStorage.setItem(this.unfinishedWizardKey, JSON.stringify(data));
    }

    switch (role) {
      case WizardExitRole.login:
        saveWizardContext(data);
        await this.router.navigateByUrl('/login');
        break;
      case WizardExitRole.dismiss:
        break;
      default:
        saveWizardContext(data);
        break;
    }
  }

  hasPendingWizardState(): boolean {
    return localStorage.getItem(this.unfinishedWizardKey)?.length > 0;
  }

  getPendingWizardState(): DeepPartial<FeedBuilder> {
    return JSON.parse(localStorage.getItem(this.unfinishedWizardKey));
  }

  resetWizardState() {
    localStorage.removeItem(this.unfinishedWizardKey);
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
    if (response.data) {
      return response.data;
    } else {
      return null;
    }
  }

  async resumeFeedWizard() {
    const feedBuilder: DeepPartial<FeedBuilder> =
      this.getPendingWizardState();
    const componentProps: FeedBuilderModalComponentProps = {
      feedBuilder: feedBuilder ?? {}
    };
    // this.resetWizardState();
    await this.openFeedBuilder(componentProps);
  }
}
