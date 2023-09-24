import {
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
} from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { TypedFormControls } from '../wizard/wizard.module';
import { GqlVisibility } from '../../../generated/graphql';
import { Subscription } from 'rxjs';
import compact from 'lodash-es/compact';
import { BucketData } from '../../graphql/types';

export type BucketFormData = {
  valid: boolean;
  data: BucketData;
};

@Component({
  selector: 'app-bucket-edit',
  templateUrl: './bucket-edit.component.html',
  styleUrls: ['./bucket-edit.component.scss'],
})
export class BucketEditComponent implements OnInit, OnDestroy {
  @Output()
  bucketData: EventEmitter<BucketFormData> = new EventEmitter<BucketFormData>();

  @Input()
  bucket?: BucketData;

  @Input()
  preview: boolean;

  formGroup: FormGroup<TypedFormControls<BucketData>>;
  visibilityEnum = GqlVisibility;
  tagsFc: FormControl<string> = new FormControl<string>('');
  private subscriptions: Subscription[] = [];

  constructor() {}

  ngOnInit() {
    this.formGroup = new FormGroup<TypedFormControls<BucketData>>({
      title: new FormControl('', [Validators.required]),
      description: new FormControl('', [Validators.required]),
      imageUrl: new FormControl(''),
      websiteUrl: new FormControl(''),
      tags: new FormControl([]),
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

    this.tagsFc.setValue(this.bucket?.tags?.join(' ') || '');
    this.tagsFc.valueChanges.subscribe((value) =>
      this.formGroup.controls.tags.setValue(
        compact(value.split(' ')).map((tag) => tag.trim()),
      ),
    );

    this.formGroup.controls.title.markAsDirty();
    this.formGroup.controls.description.markAsDirty();
    this.formGroup.controls.websiteUrl.markAsDirty();
    this.formGroup.controls.visibility.markAsDirty();

    this.subscriptions.push(
      this.formGroup.valueChanges.subscribe(() => this.bubbleUpData()),
    );

    this.bubbleUpData();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  private bubbleUpData() {
    this.bucketData.emit({
      valid: this.formGroup.valid,
      data: this.formGroup.value as BucketData,
    });
  }
}
