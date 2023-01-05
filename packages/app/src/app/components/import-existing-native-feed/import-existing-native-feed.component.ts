import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { BasicNativeFeed } from '../../services/feed.service';
import { ModalController } from '@ionic/angular';
import { ModalDismissal, ModalSuccess } from '../../app.module';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ImporterService } from '../../services/importer.service';

export interface ImportExistingNativeFeedComponentProps {
  nativeFeed: BasicNativeFeed;
  bucketId: string;
}

@Component({
  selector: 'app-import-existing-native-feed',
  templateUrl: './import-existing-native-feed.component.html',
  styleUrls: ['./import-existing-native-feed.component.scss']
})
export class ImportExistingNativeFeedComponent
  implements OnInit, ImportExistingNativeFeedComponentProps
{
  nativeFeed: BasicNativeFeed;
  bucketId: string;

  formGroup: FormGroup<{
    websiteUrl: FormControl<string>;
    description: FormControl<string | null>;
    title: FormControl<string>;
    harvest: FormControl<boolean>;
    prerender: FormControl<boolean>;
    autoRelease: FormControl<boolean>;
  }>;

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly importerService: ImporterService
  ) {}

  async ngOnInit() {
    this.formGroup = new FormGroup({
      title: new FormControl(this.nativeFeed.title, Validators.required),
      description: new FormControl(this.nativeFeed.description),
      websiteUrl: new FormControl(this.nativeFeed.websiteUrl, Validators.required),
      harvest: new FormControl(false, Validators.required),
      prerender: new FormControl(false, Validators.required),
      autoRelease: new FormControl(true, Validators.required),
    });
  }

  closeModal() {
    const response: ModalDismissal = {
      cancel: true,
    };
    return this.modalCtrl.dismiss(response);
  }

  async importAndClose() {
    if (this.formGroup.invalid) {
      console.warn(this.formGroup);
    } else {
      const values = this.formGroup.value;
      await this.importerService.createImporter({
        autoRelease: values.autoRelease,
        where: {
          id: this.bucketId,
        },
        feed: {
          connect: {
            id: this.nativeFeed.id
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
