import { Component, OnInit } from '@angular/core';
import { BucketService } from '../../services/bucket.service';
import {
  GqlBucketCreateInput,
  GqlBucketVisibility,
} from '../../../generated/graphql';
import { Router } from '@angular/router';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ModalController } from '@ionic/angular';
import { SettingsService } from '../../services/settings.service';
import { ModalDismissal } from '../../app.module';

@Component({
  selector: 'app-bucket-create',
  templateUrl: './bucket-create.page.html',
  styleUrls: ['./bucket-create.page.scss'],
})
export class BucketCreatePage implements OnInit {
  formGroup: FormGroup<{
    websiteUrl: FormControl<string | null>;
    description: FormControl<string | null>;
    name: FormControl<string | null>;
    isPublic: FormControl<boolean | null>;
  }>;
  showErrors = false;

  constructor(
    private readonly bucketService: BucketService,
    private readonly modalCtrl: ModalController,
    private readonly router: Router
  ) {
    this.formGroup = new FormGroup({
      name: new FormControl('', Validators.required),
      description: new FormControl('', Validators.required),
      websiteUrl: new FormControl(''),
      isPublic: new FormControl(true, Validators.required),
    });
  }

  ngOnInit() {}

  async createBucket() {
    if (!this.formGroup.invalid) {
      const data: GqlBucketCreateInput = {
        name: this.formGroup.value.name,
        description: this.formGroup.value.description,
        websiteUrl: this.formGroup.value.websiteUrl,
        releaseManually: false,
        visibility: GqlBucketVisibility.IsPublic,
      };
      const bucket = await this.bucketService.createBucket(data);
      const response: ModalDismissal = {
        cancel: false
      }
      await this.modalCtrl.dismiss(response);
      await this.router.navigateByUrl(`/bucket/${bucket.id}`);
    }
    this.showErrors = true;
  }
}
