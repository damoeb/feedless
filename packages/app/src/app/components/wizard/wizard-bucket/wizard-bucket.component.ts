import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import {
  WizardContext,
  WizardStepId,
} from '../wizard/wizard.component';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { TypedFormControls } from '../wizard.module';
import { BasicBucket, BucketService } from '../../../services/bucket.service';
import { ProfileService } from '../../../services/profile.service';
import { Pagination } from '../../../services/pagination.service';
import { WizardHandler } from '../wizard-handler';

interface FormMetadata {
  title: string;
  description: string;
  imageUrl: string;
  websiteUrl: string;
  tags: string;
}

@Component({
  selector: 'app-wizard-bucket',
  templateUrl: './wizard-bucket.component.html',
  styleUrls: ['./wizard-bucket.component.scss'],
})
export class WizardBucketComponent implements OnInit {
  @Input()
  handler: WizardHandler;

  @Output()
  navigateTo: EventEmitter<WizardStepId> = new EventEmitter<WizardStepId>();
  formGroup: FormGroup<TypedFormControls<FormMetadata>>;
  busyResolvingBucket: string;
  existingBuckets: Array<BasicBucket> = [];
  private pagination: Pagination;

  constructor(private readonly bucketService: BucketService,
              private readonly profileService: ProfileService) {}

  async ngOnInit() {
    this.formGroup = new FormGroup<TypedFormControls<FormMetadata>>(
      {
        title: new FormControl('', [Validators.required]),
        description: new FormControl('', [Validators.required]),
        imageUrl: new FormControl('', [Validators.required]),
        websiteUrl: new FormControl('', []),
        tags: new FormControl('', []),
      },
      { updateOn: 'change' }
    );
    // await this.searchBuckets();
  }

  async searchBuckets() {
    const {buckets, pagination} = await this.bucketService.search({
      where: {
        ownerId: this.profileService.getUserId()
      },
      page: 0
    });
    this.existingBuckets = buckets;
    this.pagination = pagination;
  }
}
