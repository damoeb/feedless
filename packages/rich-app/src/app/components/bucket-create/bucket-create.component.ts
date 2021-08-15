import { Component, OnInit } from '@angular/core';
import { BucketService } from '../../services/bucket.service';
import { ToastService } from '../../services/toast.service';
import { ModalController } from '@ionic/angular';

@Component({
  selector: 'app-bucket-create',
  templateUrl: './bucket-create.component.html',
  styleUrls: ['./bucket-create.component.scss'],
})
export class BucketCreateComponent implements OnInit {
  title: string = '';

  constructor(
    private readonly bucketService: BucketService,
    private readonly modalController: ModalController,
    private readonly toastService: ToastService
  ) {}

  ngOnInit() {}

  createBucket() {
    if (!this.isValid()) {
      this.toastService.info('Enter title');
      return;
    }
    this.bucketService
      .createBucket(this.title)
      .toPromise()
      .then(({ data, errors }) => {
        if (errors) {
          this.toastService.errors(errors);
        } else {
          this.toastService.info('Created');
          this.modalController.dismiss(data.createBucket.id);
        }
      });
  }
  async dismissModal() {
    await this.modalController.dismiss();
  }

  isValid(): boolean {
    return this.title.length > 0;
  }
}
