import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Bucket, BucketService } from '../../services/bucket.service';
import { ActivatedRoute, Router } from '@angular/router';
import { BubbleColor } from '../bubble/bubble.component';
import { NativeFeed } from '../../services/feed.service';
import { ActionSheetController, ModalController, ToastController } from '@ionic/angular';
import { ImporterCreatePage } from '../importer-create/importer-create.page';
import { Importer } from '../../services/importer.service';
import { FieldWrapper, GqlGenericFeed, GqlImporter, GqlNativeFeed, Scalars } from '../../../generated/graphql';

@Component({
  selector: 'app-bucket-edit',
  templateUrl: './bucket-edit.page.html',
  styleUrls: ['./bucket-edit.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BucketEditPage implements OnInit {

  formGroup: FormGroup<{ website: FormControl<string | null>; description: FormControl<string | null>; name: FormControl<string | null> }>;
  bucket: Bucket;

  constructor(private readonly bucketService: BucketService,
              private readonly activatedRoute: ActivatedRoute,
              private readonly toastCtrl: ToastController,
              private readonly router: Router,
              private readonly actionSheetCtrl: ActionSheetController,
              private readonly modalController: ModalController,
              private readonly changeRef: ChangeDetectorRef) {
    this.formGroup = new FormGroup({
      name: new FormControl('', Validators.required),
      description: new FormControl('', Validators.required),
      website: new FormControl('', Validators.required),
    });
  }

  ngOnInit() {
    this.activatedRoute.params.subscribe(params => {
      this.initBucket(params.id);
    })
  }

  private async initBucket(bucketId: string) {
    this.bucket = await this.bucketService.getBucketById(bucketId);
    this.changeRef.detectChanges();
  }

  async showOptions() {
    const actionSheet = await this.actionSheetCtrl.create({
      buttons: [
        {
          text: 'Delete',
          role: 'destructive',
          handler: () => {
            this.deleteBucket();
          }
        },
        {
          text: 'Cancel',
          role: 'cancel',
          data: {
            action: 'cancel',
          },
        },
      ],
    });

    await actionSheet.present();

    const result = await actionSheet.onDidDismiss();

  }

  getColorForImporter(active: Boolean, status: string): BubbleColor {
    if (active) {
      if (status === 'OK') {
        return 'green'
      } else {
        return 'red'
      }
    } else {
      return 'gray'
    }
  }

  lastUpdatedAt(lastUpdatedAt: number): Date {
    return new Date(lastUpdatedAt);
  }

  async openImporterModal() {
    const modal = await this.modalController.create({
      component: ImporterCreatePage,
      componentProps: {
        bucket: this.bucket
      }
    });
    await modal.present();
  }

  private async deleteBucket() {
    await this.bucketService.deleteBucket(this.bucket.id);
    const toast = await this.toastCtrl.create({
      message: 'Deleted',
      duration: 3000,
      color: 'success',
    });

    await toast.present();
    await this.router.navigateByUrl('/')

  }
}
