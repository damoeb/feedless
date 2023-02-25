import { Component, OnInit, ViewChild } from '@angular/core';
import { BasicNativeFeed } from '../../services/feed.service';
import { ModalController, ToastController } from '@ionic/angular';
import { ModalDismissal, ModalSuccess } from '../../app.module';
import { ImporterService } from '../../services/importer.service';
import { FeedMetadata } from '../feed-metadata-form/feed-metadata-form.component';
import { ImporterMetadataFormComponent } from '../importer-metadata-form/importer-metadata-form.component';

export interface ImportExistingNativeFeedComponentProps {
  nativeFeed: BasicNativeFeed;
  bucketId: string;
}

@Component({
  selector: 'app-import-existing-native-feed',
  templateUrl: './import-existing-native-feed.component.html',
  styleUrls: ['./import-existing-native-feed.component.scss'],
})
export class ImportExistingNativeFeedComponent
  implements OnInit, ImportExistingNativeFeedComponentProps
{
  @ViewChild('importerMetadataFormComponent')
  importerMetadataFormComponent: ImporterMetadataFormComponent;

  nativeFeed: BasicNativeFeed;
  bucketId: string;

  feedMetadata: FeedMetadata;

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly importerService: ImporterService,
    private readonly toastCtrl: ToastController
  ) {}

  async ngOnInit() {
    const feed = this.nativeFeed;
    this.feedMetadata = {
      title: feed.title,
      description: feed.description,
      websiteUrl: feed.websiteUrl,
      autoRelease: false,
      harvestItems: false,
      prerender: false,
      language: '',
    };
  }

  closeModal() {
    const response: ModalDismissal = {
      cancel: true,
    };
    return this.modalCtrl.dismiss(response);
  }

  async importAndClose() {
    const formGroup = this.importerMetadataFormComponent.formGroup;
    if (formGroup.invalid) {
      const toast = await this.toastCtrl.create({
        message: 'Form is incomplete',
        duration: 4000,
        color: 'danger',
      });
      await toast.present();
    } else {
      const { autoImport } = formGroup.value;
      await this.importerService.createImporter({
        autoRelease: autoImport,
        where: {
          id: this.bucketId,
        },
        feed: {
          connect: {
            id: this.nativeFeed.id,
          },
        },
      });

      const response: ModalSuccess = {
        cancel: false,
      };
      return this.modalCtrl.dismiss(response);
    }
  }
}
