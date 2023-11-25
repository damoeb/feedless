import { Component } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { BucketFormData } from '../../components/bucket-edit/bucket-edit.component';
import { Bucket, BucketData } from '../../graphql/types';

export interface BucketCreateModalComponentProps {
}

@Component({
  selector: 'app-bucket-create-modal',
  templateUrl: './bucket-create-modal.component.html',
  styleUrls: ['./bucket-create-modal.component.scss'],
})
export class BucketCreateModalComponent
  implements BucketCreateModalComponentProps
{
  canSubmit: boolean;

  bucket: Bucket;

  private data: BucketData;

  constructor(private readonly modalCtrl: ModalController) {}

  dismissModal() {
    return this.modalCtrl.dismiss();
  }

  handleBucketData(formData: BucketFormData) {
    this.canSubmit = formData.valid;
    this.data = formData.data;
  }

  applyChanges() {
    return this.modalCtrl.dismiss();
  }
}
