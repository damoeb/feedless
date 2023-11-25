import {
  Component,
  EventEmitter, forwardRef,
  Input,
  OnDestroy,
  OnInit,
  Output
} from '@angular/core';
import { FormControl, FormGroup, NG_VALUE_ACCESSOR, Validators } from '@angular/forms';
import { TypedFormControls } from '../wizard/wizard.module';
import { GqlVisibility } from '../../../generated/graphql';
import { Subscription } from 'rxjs';
import compact from 'lodash-es/compact';
import { BucketData } from '../../graphql/types';
import { ControlValueAccessorDirective } from '../../directives/control-value-accessor/control-value-accessor.directive';

export type BucketFormData = {
  valid: boolean;
  data: BucketData;
};

@Component({
  selector: 'app-bucket-edit',
  templateUrl: './bucket-edit.component.html',
  styleUrls: ['./bucket-edit.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => BucketEditComponent),
      multi: true,
    },
  ]
})
export class BucketEditComponent <T> extends ControlValueAccessorDirective<T> implements OnInit, OnDestroy {

  @Input()
  preview: boolean;

  formGroup: FormGroup<TypedFormControls<BucketData>>;
  visibilityEnum = GqlVisibility;
  tagsFc: FormControl<string> = new FormControl<string>('');
  private subscriptions: Subscription[] = [];

  ngOnInit() {
    super.ngOnInit();
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

    this.subscriptions.push(
      this.control.valueChanges.subscribe(bucket => {
        this.formGroup.patchValue({
          title: bucket?.title,
          description: bucket?.description,
          imageUrl: bucket?.imageUrl,
          websiteUrl: bucket?.websiteUrl,
          tags: bucket?.tags,
          visibility: GqlVisibility.IsPublic,
        });
        this.tagsFc.setValue(bucket?.tags?.join(' ') ?? '');
        this.tagsFc.valueChanges.subscribe((value) =>
          this.formGroup.controls.tags.setValue(
            compact(value.split(' ')).map((tag) => tag.trim()),
          ),
        );
      })
    );

    this.subscriptions.push(
      this.formGroup.valueChanges.subscribe(() => this.bubbleUpData()),
    );

    this.bubbleUpData();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  private bubbleUpData() {
    // this.bucketData.emit({
    //   valid: this.formGroup.valid,
    //   data: this.formGroup.value as BucketData,
    // });
  }
}
