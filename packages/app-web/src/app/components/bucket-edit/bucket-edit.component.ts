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
import { isEqual } from 'lodash-es';

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
      title: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(3)] }),
      description: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(10)] }),
      imageUrl: new FormControl(''),
      websiteUrl: new FormControl(''),
      tags: new FormControl([]),
      visibility: new FormControl<GqlVisibility>(GqlVisibility.IsPublic, [
        Validators.required,
      ]),
    });

    this.subscriptions.push(
      this.control.valueChanges.subscribe(bucket => {
        if (!isEqual(bucket, this.formGroup.value)) {
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
        }
      })
    );

    this.subscriptions.push(
      this.formGroup.statusChanges.subscribe((status) => {
        console.log('status', status);
        if (status === 'VALID') {
          this.control.setErrors(null)
        } else {
          this.control.setErrors({
            'bucket': 'invalid'
          });
        }
      }),
      this.formGroup.valueChanges.subscribe((bucket) => {
        this.control.setValue(bucket)
      }),
    );

  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
