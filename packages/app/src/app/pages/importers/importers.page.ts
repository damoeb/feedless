import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
} from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Bucket, BucketService } from '../../services/bucket.service';
import { ActivatedRoute, Router } from '@angular/router';
import {
  ActionSheetController,
  ModalController,
  ToastController,
} from '@ionic/angular';
import {
  ImporterCreatePage,
  ImporterCreatePageProps,
} from '../../components/importer-create/importer-create.page';
import { ModalDismissal } from '../../app.module';
import { GqlGenericFeed, Maybe } from '../../../generated/graphql';
import { BasicNativeFeed } from '../../services/feed.service';
import { BasicImporter } from '../../services/importer.service';
import { without } from 'lodash';

type Importer = BasicImporter & {
  nativeFeed: BasicNativeFeed & {
    genericFeed?: Maybe<Pick<GqlGenericFeed, 'id'>>;
  };
};

@Component({
  selector: 'app-bucket-edit',
  templateUrl: './importers.page.html',
  styleUrls: ['./importers.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ImportersPage implements OnInit {
  formGroup: FormGroup<{
    website: FormControl<string | null>;
    description: FormControl<string | null>;
    name: FormControl<string | null>;
  }>;
  bucket: Bucket;
  checkedImporters: Importer[] = [];

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

  // getColorForImporter(active: boolean, status: string): BubbleColor {
  //   if (active) {
  //     if (status === 'OK') {
  //       return 'green';
  //     } else {
  //       return 'red';
  //     }
  //   } else {
  //     return 'gray';
  //   }
  // }

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
      backdropDismiss: false,
    });
    await modal.present();
    await modal.onDidDismiss<ModalDismissal>();
    await this.initBucket(this.bucket.id);
  }

  toggleCheckAll(event: any) {
    if (event.detail.checked) {
      this.checkedImporters = [...this.bucket.importers];
    } else {
      this.checkedImporters = [];
    }
  }

  onCheckChange(event: any, importer: Importer) {
    if (event.detail.checked) {
      this.checkedImporters.push(importer);
    } else {
      this.checkedImporters = without(this.checkedImporters, importer);
    }
  }

  isChecked(importer: Importer): boolean {
    return this.checkedImporters.indexOf(importer) > -1;
  }

  async showActions() {
    const actionSheet = await this.actionSheetCtrl.create({
      header: `Actions for ${this.checkedImporters.length} Articles`,
      buttons: [
        {
          text: 'Delete',
          role: 'destructive',
          handler: () => {
            // todo mag
          },
        },
        {
          text: 'Publish',
          role: 'destructive',
          handler: () => {
            // todo mag
          },
        },
        {
          text: 'Retract',
          role: 'destructive',
          handler: () => {
            // todo mag
          },
        },
      ],
    });

    await actionSheet.present();

    const result = await actionSheet.onDidDismiss();
  }

  private async initBucket(bucketId: string) {
    this.bucket = await this.bucketService.getBucketById(bucketId);
    this.changeRef.detectChanges();
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
