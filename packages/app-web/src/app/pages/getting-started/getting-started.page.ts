import { Component, ViewChild } from '@angular/core';
import { WizardService } from '../../services/wizard.service';
import { GqlPuppeteerWaitUntil } from '../../../generated/graphql';
import { PageHeaderComponent } from '../../components/page-header/page-header.component';
import '@justinribeiro/lite-youtube';
import { Router } from '@angular/router';
import { WizardComponent, WizardComponentProps } from '../../components/wizard/wizard/wizard.component';
import { ModalController } from '@ionic/angular';
import { FeedBuilderModalComponent } from '../../modals/feed-builder-modal/feed-builder-modal.component';

export const isUrl = (value: string): boolean => {
  if (!value || value.length < 3) {
    return false;
  }
  const potentialUrl = value.toLowerCase();
  if (
    potentialUrl.startsWith('http://') ||
    potentialUrl.startsWith('https://')
  ) {
    try {
      new URL(value);

      const urlPattern =
        /[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{2,6}\b([-a-zA-Z0-9()@:%_\+.~#?&//=]*)?/gi;
      return !!potentialUrl.match(new RegExp(urlPattern));
    } catch (e) {
      return false;
    }
  } else {
    return isUrl(`https://${potentialUrl}`);
  }
};

export const fixUrl = (value: string): string => {
  const potentialUrl = value.trim();
  if (
    potentialUrl.toLowerCase().startsWith('http://') ||
    potentialUrl.toLowerCase().startsWith('https://')
  ) {
    return potentialUrl;
  } else {
    return `https://${potentialUrl}`;
  }
};

@Component({
  selector: 'app-getting-started',
  templateUrl: './getting-started.page.html',
  styleUrls: ['./getting-started.page.scss'],
})
export class GettingStartedPage {
  @ViewChild('headerComponent')
  headerComponent: PageHeaderComponent;

  constructor(
    private readonly wizardService: WizardService,
    private readonly modalCtrl: ModalController,
    private readonly router: Router,
  ) {}

  async openWizard(url: string) {
    console.log('openWizard');
    if (isUrl(url)) {
      await this.wizardService.openFeedWizard({
        fetchOptions: {
          websiteUrl: fixUrl(url),
          prerender: false,
          prerenderWaitUntil: GqlPuppeteerWaitUntil.Load,
        },
      });
      this.headerComponent.refresh();
    }
  }

  openReader(url: string) {
    console.log('openReader');
    return this.router.navigateByUrl(`/reader?url=${fixUrl(url)}`);
  }

  async openFeedBuilder() {
    const componentProps = {};
    const modal = await this.modalCtrl.create({
      component: FeedBuilderModalComponent,
      componentProps,
      cssClass: 'modal-dialog',
      showBackdrop: true,
      backdropDismiss: false,
    });
    await modal.present();
  }
}
