import { Component, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { Bucket } from '../../graphql/types';
import { FormControl } from '@angular/forms';

export interface BucketCreateModalComponentProps {}

@Component({
  selector: 'app-bucket-create-modal',
  templateUrl: './bucket-create-modal.component.html',
  styleUrls: ['./bucket-create-modal.component.scss'],
})
export class BucketCreateModalComponent
  implements BucketCreateModalComponentProps, OnInit
{
  bucketFc: FormControl<Bucket | null>;

  constructor(private readonly modalCtrl: ModalController) {}

  dismissModal() {
    return this.modalCtrl.dismiss();
  }

  applyChanges() {
    console.log('applyChanges', this.bucketFc.value);
    return this.modalCtrl.dismiss(this.bucketFc.value);
  }

  ngOnInit(): void {
    this.bucketFc = new FormControl<Bucket>(null);
  }
}
