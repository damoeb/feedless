import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnInit,
} from '@angular/core';
import { TypedFormControls } from '../wizard.module';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { EmbedWebsite } from '../../embedded-website/embedded-website.component';
import { WizardHandler } from '../wizard-handler';
import { debounce, interval } from 'rxjs';

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
  handler: WizardHandler;

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

    this.formGroup.valueChanges
      .pipe(debounce(() => interval(500)))
      .subscribe(() => {});

    const discovery = this.handler.getDiscovery();
    this.embedWebsiteData = {
      htmlBody: discovery.document.htmlBody,
      mimeType: discovery.document.mimeType,
      url: discovery.websiteUrl,
    };
  }
}
