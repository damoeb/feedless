import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { TypedFormControls } from '../wizard/wizard.module';
import {
  GqlBucketCreateInput,
  GqlVisibility,
} from '../../../generated/graphql';

export type BucketData = Pick<
  GqlBucketCreateInput,
  'description' | 'imageUrl' | 'tags' | 'title' | 'visibility' | 'websiteUrl'
>;

export type BucketFormData = {
  valid: boolean;
  data: BucketData;
};

@Component({
  selector: 'app-bucket-edit',
  templateUrl: './bucket-edit.component.html',
  styleUrls: ['./bucket-edit.component.scss'],
})
export class BucketEditComponent implements OnInit {
  @Output()
  bucketData: EventEmitter<BucketFormData> = new EventEmitter<BucketFormData>();

  @Input()
  bucket?: BucketData;

  @Input()
  preview: boolean;

  formGroup: FormGroup<TypedFormControls<BucketData>>;
  visibilityEnum = GqlVisibility;

  constructor() {}

  ngOnInit() {
    this.formGroup = new FormGroup<TypedFormControls<BucketData>>({
      title: new FormControl('', [Validators.required]),
      description: new FormControl('', [Validators.required]),
      imageUrl: new FormControl(''),
      websiteUrl: new FormControl(''),
      tags: new FormControl(''),
      visibility: new FormControl<GqlVisibility>(GqlVisibility.IsPublic, [
        Validators.required,
      ]),
    });

    this.formGroup.patchValue({
      title: this.bucket?.title,
      description: this.bucket?.description,
      imageUrl: this.bucket?.imageUrl,
      websiteUrl: this.bucket?.websiteUrl,
      tags: this.bucket?.tags,
      visibility: GqlVisibility.IsPublic,
    });

    this.formGroup.controls.title.markAsDirty();
    this.formGroup.controls.description.markAsDirty();
    this.formGroup.controls.websiteUrl.markAsDirty();
    this.formGroup.controls.visibility.markAsDirty();

    this.formGroup.valueChanges.subscribe(() => this.bubbleUpData());

    this.bubbleUpData();
  }

  private bubbleUpData() {
    this.bucketData.emit({
      valid: this.formGroup.valid,
      data: this.formGroup.value as BucketData,
    });
  }
}
