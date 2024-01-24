import { Injectable } from '@angular/core';
import { CodeEditorModalComponent, CodeEditorModalComponentProps } from '../modals/code-editor-modal/code-editor-modal.component';
import { ModalController } from '@ionic/angular';
import {
  DeepPartial,
  FeedBuilder,
  FeedBuilderModalComponent,
  FeedBuilderModalComponentExitRole,
  FeedBuilderModalComponentProps,
  FeedBuilderModalData
} from '../modals/feed-builder-modal/feed-builder-modal.component';
import { AgentsModalComponent, AgentsModalComponentProps } from '../modals/agents-modal/agents-modal.component';
import { Agent } from './agent.service';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class ModalService {
  private readonly unfinishedWizardKey = 'unfinished-wizard';

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly router: Router
  ) {
  }

  async openCodeEditorModal(code: string = null): Promise<FeedBuilder | null> {
    const componentProps: CodeEditorModalComponentProps = {
      code: code ?? ''
    };
    const modal = await this.modalCtrl.create({
      component: CodeEditorModalComponent,
      componentProps
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
    overwriteHandler: (data: FeedBuilder, role: String) => Promise<void> = null
  ) {
    const modal = await this.modalCtrl.create({
      component: FeedBuilderModalComponent,
      componentProps,
      cssClass: 'modal-dialog',
      showBackdrop: true,
      backdropDismiss: false
    });
    await modal.present();
    const { data, role } = await modal.onDidDismiss<FeedBuilderModalData>();

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
    componentProps: AgentsModalComponentProps
  ): Promise<Agent | null> {
    const modal = await this.modalCtrl.create({
      component: AgentsModalComponent,
      componentProps,
      cssClass: 'modal-dialog',
      showBackdrop: true,
      backdropDismiss: false
    });
    await modal.present();

    const response = await modal.onDidDismiss<Agent | null>();
    if (response.data) {
      return response.data;
    } else {
      return null;
    }
  }

  async resumeFeedWizard() {
    const feedBuilder: DeepPartial<FeedBuilder> = this.getPendingWizardState();
    const componentProps: FeedBuilderModalComponentProps = {
      feedBuilder: feedBuilder ?? {}
    };
    // this.resetWizardState();
    await this.openFeedBuilder(componentProps);
  }
}
