import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
} from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Bucket, BucketService } from '../../services/bucket.service';
import { ActivatedRoute, Router } from '@angular/router';
import { BubbleColor } from '../bubble/bubble.component';
import {
  ActionSheetController,
  ModalController,
  ToastController,
} from '@ionic/angular';
import { ImporterCreatePage, ImporterCreatePageProps } from '../importer-create/importer-create.page';
import { ModalDismissal } from '../../app.module';

@Component({
  selector: 'app-bucket-edit',
  templateUrl: './bucket-feeds.page.html',
  styleUrls: ['./bucket-feeds.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BucketFeedsPage implements OnInit {
  formGroup: FormGroup<{
    website: FormControl<string | null>;
    description: FormControl<string | null>;
    name: FormControl<string | null>;
  }>;
  bucket: Bucket;

  constructor(
    private readonly bucketService: BucketService,
    private readonly activatedRoute: ActivatedRoute,
    private readonly toastCtrl: ToastController,
    private readonly router: Router,
    private readonly actionSheetCtrl: ActionSheetController,
    private readonly modalCtrl: ModalController,
    private readonly changeRef: ChangeDetectorRef
  ) {
    this.formGroup = new FormGroup({
      name: new FormControl('', Validators.required),
      description: new FormControl('', Validators.required),
      website: new FormControl('', Validators.required),
    });
  }

  ngOnInit() {
    this.activatedRoute.params.subscribe((params) => {
      this.initBucket(params.id);
    });
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
          },
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
    await actionSheet.onDidDismiss();
  }

  getColorForImporter(active: Boolean, status: string): BubbleColor {
    if (active) {
      if (status === 'OK') {
        return 'green';
      } else {
        return 'red';
      }
    } else {
      return 'gray';
    }
  }

  toDate(lastUpdatedAt: number): Date {
    return new Date(lastUpdatedAt);
  }

  async openImporterModal() {
    const componentProps: ImporterCreatePageProps = {
      bucket: this.bucket,
    };
    const modal = await this.modalCtrl.create({
      component: ImporterCreatePage,
      componentProps,
      backdropDismiss: false
    });
    await modal.present();
    await modal.onDidDismiss<ModalDismissal>();
    await this.initBucket(this.bucket.id);
  }

  private async deleteBucket() {
    await this.bucketService.deleteBucket(this.bucket.id);
    const toast = await this.toastCtrl.create({
      message: 'Deleted',
      duration: 3000,
      color: 'success',
    });

    await toast.present();
    await this.router.navigateByUrl('/');
  }
}
