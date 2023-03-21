import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { TypedFormControls } from '../wizard/wizard.module';
import {
  GqlBucketCreateInput,
  GqlVisibility,
} from '../../../generated/graphql';
import { debounce, interval } from 'rxjs';

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

  formGroup: FormGroup<TypedFormControls<BucketData>>;

  constructor() {}

  ngOnInit() {
    this.formGroup = new FormGroup<TypedFormControls<BucketData>>(
      {
        title: new FormControl(this.bucket?.title, [Validators.required]),
        description: new FormControl(this.bucket?.description, [
          Validators.required,
        ]),
        imageUrl: new FormControl(this.bucket?.imageUrl),
        websiteUrl: new FormControl(this.bucket?.websiteUrl),
        tags: new FormControl(this.bucket?.tags),
        visibility: new FormControl<GqlVisibility>(GqlVisibility.IsPublic, []),
      },
      { updateOn: 'change' }
    );

    this.formGroup.valueChanges
      .pipe(debounce(() => interval(500)))
      .subscribe(() => this.tryEmit());

    this.tryEmit();
  }

  private tryEmit() {
    if (this.formGroup.valid) {
      this.bucketData.emit({
        valid: this.formGroup.valid,
        data: this.formGroup.value as BucketData,
      });
    }
  }
}
