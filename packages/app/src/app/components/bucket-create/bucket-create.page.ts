import { Component, OnInit } from '@angular/core';
import { BucketService } from '../../services/bucket.service';
import { GqlBucketCreateInput, GqlBucketVisibility } from '../../../generated/graphql';
import { Router } from '@angular/router';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ModalController } from '@ionic/angular';
import { SettingsService } from '../../services/settings.service';

@Component({
  selector: 'app-bucket-create',
  templateUrl: './bucket-create.page.html',
  styleUrls: ['./bucket-create.page.scss'],
})
export class BucketCreatePage implements OnInit {
  formGroup: FormGroup<{ websiteUrl: FormControl<string | null>; description: FormControl<string | null>; name: FormControl<string | null>; isPublic: FormControl<boolean | null> }>;
  showErrors = false;

  constructor(private readonly bucketService: BucketService,
              private readonly modalController: ModalController,
              private readonly settingsService: SettingsService,
              private readonly router: Router) {
    this.formGroup = new FormGroup({
      name: new FormControl('', Validators.required),
      description: new FormControl('', Validators.required),
      websiteUrl: new FormControl(''),
      isPublic: new FormControl(true, Validators.required),
    });
  }

  ngOnInit() {
  }

  async createBucket() {
    console.log(this.formGroup);
    if(!this.formGroup.invalid) {
      const data: GqlBucketCreateInput = {
        corrId: this.settingsService.getCorrId(),
        name: this.formGroup.value.name,
        description: this.formGroup.value.description,
        websiteUrl: this.formGroup.value.websiteUrl,
        releaseManually: false,
        visibility: GqlBucketVisibility.IsPublic
      };
      const bucket = await this.bucketService.createBucket(data);
      await this.modalController.dismiss();
      await this.router.navigateByUrl(`/bucket/${bucket.id}`)
    }
    this.showErrors = true;
  }
}
