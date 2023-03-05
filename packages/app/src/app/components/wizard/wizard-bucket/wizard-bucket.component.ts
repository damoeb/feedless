import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { WizardContext, WizardStepId } from '../wizard/wizard.component';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { TypedFormControls } from '../wizard.module';

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
  context: WizardContext;

  @Output()
  updateContext: EventEmitter<Partial<WizardContext>> = new EventEmitter<
    Partial<WizardContext>
  >();
  @Output()
  navigateTo: EventEmitter<WizardStepId> = new EventEmitter<WizardStepId>();
  formGroup: FormGroup<TypedFormControls<FormMetadata>>;

  constructor() {}

  ngOnInit() {
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
  }
}
