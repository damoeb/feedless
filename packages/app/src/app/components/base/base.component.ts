import { Component } from '@angular/core';
import { GqlBucket, GqlNotebook } from '../../../generated/graphql';
import { BucketService } from '../../services/bucket.service';
import { Router } from '@angular/router';
import { ModalController } from '@ionic/angular';
import { ToastService } from '../../services/toast.service';
import { BucketCreateComponent } from '../bucket-create/bucket-create.component';

@Component({
  selector: 'app-base',
  templateUrl: './base.component.html',
  styleUrls: ['./base.component.scss'],
})
export class BaseComponent {
  public buckets: GqlBucket[] = [];
  public notebooks: GqlNotebook[] = [];

  constructor(
    private readonly bucketService: BucketService,
    private readonly router: Router,
    private readonly modalController: ModalController,
    private readonly toastService: ToastService
  ) {
    this.bucketService
      .getBucketsForUser()
      .valueChanges.subscribe(({ data, error, loading }) => {
        if (loading) {
        } else if (error) {
          toastService.errorFromApollo(error);
        } else {
          console.log(data);

          this.notebooks = data.findFirstUser.notebooks;
          this.buckets = data.findFirstUser.buckets;
        }
      });
  }

  bucketRequiresAction(bucket: GqlBucket): boolean {
    return (
      bucket.subscriptions?.length === 0 ||
      bucket.subscriptions.some((subscription) => subscription.feed.broken)
    );
  }

  async createBucket() {
    const modal = await this.modalController.create({
      component: BucketCreateComponent,
    });
    await modal.present();
    const responseBucket = await modal.onDidDismiss<string>();
    if (responseBucket.data) {
      const bucketId = responseBucket.data;
      await this.router.navigate([`/bucket/${bucketId}/edit`]);
    }
  }

  // @HostListener('window:resize', ['$event'])
  // onFocus(event) {
  //   navigator.clipboard
  //     .readText()
  //     .then((text) => {
  //       console.log('Pasted content: ', text);
  //     })
  //     .catch((err) => {
  //       console.error('Failed to read clipboard contents: ', err);
  //     });
  // }

  getNotebooks(): GqlNotebook[] {
    return this.notebooks.filter((notebook) => !notebook.readonly);
  }
}
