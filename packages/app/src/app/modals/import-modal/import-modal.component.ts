import { Component } from '@angular/core';
import { OpmlService, Outline } from '../../services/opml.service';
import { ModalController, ToastController } from '@ionic/angular';
import {
  ImportOpmlModalComponent,
  ImportOpmlModalComponentProps,
} from '../import-opml-modal/import-opml-modal.component';
import {
  ImportFromMarkupModalComponent,
  ImportFromMarkupModalComponentProps,
} from '../import-from-markup-modal/import-from-markup-modal.component';
import {
  WizardComponent,
  WizardComponentProps,
  WizardContext,
  WizardStepId,
} from '../../components/wizard/wizard/wizard.component';
import { ImporterService } from '../../services/importer.service';
import {
  GqlNativeFeedCreateOrConnectInput,
  GqlVisibility,
} from '../../../generated/graphql';

@Component({
  selector: 'app-import-modal',
  templateUrl: './import-modal.component.html',
  styleUrls: ['./import-modal.component.scss'],
})
export class ImportModalComponent {
  constructor(
    private readonly opmlService: OpmlService,
    private readonly importerService: ImporterService,
    private readonly toastCtrl: ToastController,
    private readonly modalCtrl: ModalController
  ) {}

  async uploadOpmlFile(uploadEvent: Event) {
    const componentProps: ImportOpmlModalComponentProps = {
      outlines: await this.opmlService.convertOpmlToJson(uploadEvent),
    };
    const opmlModal = await this.modalCtrl.create({
      component: ImportOpmlModalComponent,
      componentProps,
      backdropDismiss: true,
    });
    await opmlModal.present();
    const opmlModalResponse = await opmlModal.onDidDismiss<Outline[]>();
    const outlines = opmlModalResponse.data;
    console.log('outlines', opmlModalResponse.role, outlines);

    if (outlines && outlines.length === 0) {
      await this.showCancelMessage('No feed urls found');
    } else {
      if (opmlModalResponse.role) {
        const chooseBucketProps: WizardComponentProps = {
          initialContext: {
            modalTitle: 'Pick a Bucket',
            currentStepId: WizardStepId.bucket,
            bucket: {
              create: {
                title: 'New Bucket',
                description: '',
                visibility: GqlVisibility.IsPublic,
              },
            },
          },
        };
        const bucketModal = await this.modalCtrl.create({
          component: WizardComponent,
          componentProps: chooseBucketProps,
          backdropDismiss: false,
        });
        await bucketModal.present();
        const bucketModalResponse =
          await bucketModal.onDidDismiss<WizardContext>();

        if (bucketModalResponse.role) {
          const wizardContext = bucketModalResponse.data;

          await this.importerService.createImporters({
            bucket: wizardContext.bucket,
            feeds: outlines.map<GqlNativeFeedCreateOrConnectInput>(
              (outline) => ({
                create: {
                  nativeFeed: {
                    feedUrl: outline.xmlUrl,
                    title: outline.title,
                    description: outline.text,
                    websiteUrl: outline.htmlUrl,
                  },
                },
              })
            ),
          });
          const toast = await this.toastCtrl.create({
            message: `Imported ${outlines.length} feeds`,
            duration: 3000,
            color: 'success',
          });
          await toast.present();
        } else {
          await this.showCancelMessage();
        }
      } else {
        await this.showCancelMessage();
      }
    }
  }

  closeModal() {
    this.modalCtrl.dismiss();
  }

  async importYoutube() {
    const componentProps: ImportFromMarkupModalComponentProps = {
      kind: 'youtube',
      urlExtractor: (markup) => [],
    };
    const modal = await this.modalCtrl.create({
      component: ImportFromMarkupModalComponent,
      componentProps,
      backdropDismiss: true,
    });
    await modal.present();
  }

  async importTwitter() {
    const componentProps: ImportFromMarkupModalComponentProps = {
      kind: 'twitter',
      urlExtractor: (markup) => [],
    };
    const modal = await this.modalCtrl.create({
      component: ImportFromMarkupModalComponent,
      componentProps,
      backdropDismiss: true,
    });
    await modal.present();
  }

  private async showCancelMessage(msg?: string) {
    const toast = await this.toastCtrl.create({
      message: msg || 'Canceled',
      duration: 3000,
      color: 'danger',
    });
    await toast.present();
  }
}
