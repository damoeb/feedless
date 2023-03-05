import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
} from '@angular/core';
import { WizardContext } from '../wizard/wizard.component';
import { TypedFormControls } from '../wizard.module';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { EmbedWebsite } from '../../embedded-website/embedded-website.component';

interface PageChange {
  compare: string;
  output: string;
  fragmentXPath: string;
}

@Component({
  selector: 'app-wizard-page-change',
  templateUrl: './wizard-page-change.component.html',
  styleUrls: ['./wizard-page-change.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WizardPageChangeComponent implements OnInit {
  @Input()
  context: WizardContext;

  @Output()
  updateContext: EventEmitter<Partial<WizardContext>> = new EventEmitter<
    Partial<WizardContext>
  >();

  formGroup: FormGroup<TypedFormControls<PageChange>>;
  embedWebsiteData: EmbedWebsite;

  constructor() {}

  ngOnInit() {
    this.formGroup = new FormGroup<TypedFormControls<PageChange>>(
      {
        compare: new FormControl('', [Validators.required]),
        output: new FormControl('', [Validators.required]),
        fragmentXPath: new FormControl('', [Validators.required]),
      },
      { updateOn: 'change' }
    );

    this.formGroup.valueChanges.subscribe(() => {});

    this.embedWebsiteData = {
      htmlBody: this.context.discovery.document.htmlBody,
      mimeType: this.context.discovery.document.mimeType,
      url: this.context.discovery.websiteUrl,
    };
  }
}
