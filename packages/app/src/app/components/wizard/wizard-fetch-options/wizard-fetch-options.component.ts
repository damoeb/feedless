import { Component, Input, OnInit } from '@angular/core';
import { WizardContext } from '../wizard/wizard.component';
import { FeedService } from '../../../services/feed.service';
import { GqlPuppeteerWaitUntil } from '../../../../generated/graphql';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { TypedFormControls } from '../wizard.module';
import { LabelledSelectOption } from '../../feed-discovery-wizard/feed-discovery-wizard.component';
import { ModalController } from '@ionic/angular';
import { interval, throttle } from 'rxjs';
import { WizardHandler } from '../wizard-handler';

@Component({
  selector: 'app-wizard-fetch-options',
  templateUrl: './wizard-fetch-options.component.html',
  styleUrls: ['./wizard-fetch-options.component.scss'],
})
export class WizardFetchOptionsComponent implements OnInit {
  @Input()
  handler: WizardHandler;

  @Input()
  options = true;

  formGroup: FormGroup<
    TypedFormControls<
      Pick<
        WizardContext,
        'url' | 'prerender' | 'prerenderWaitUntil' | 'prerenderScript'
      >
    >
  >;

  busyResolvingUrl = false;

  constructor(
    private readonly feedService: FeedService,
    private readonly modalCtrl: ModalController
  ) {}

  ngOnInit() {
    const context = this.handler.getContext();
    this.formGroup = new FormGroup<
      TypedFormControls<
        Pick<
          WizardContext,
          'url' | 'prerender' | 'prerenderWaitUntil' | 'prerenderScript'
        >
      >
    >(
      {
        url: new FormControl(context.url, [Validators.required]),
        prerender: new FormControl(context.prerender, [Validators.required]),
        prerenderScript: new FormControl(context.prerenderScript, []),
        prerenderWaitUntil: new FormControl(context.prerenderWaitUntil, [
          Validators.required,
        ]),
      },
      { updateOn: 'change' }
    );

    this.formGroup.valueChanges
      .pipe(throttle(() => interval(1000)))
      .subscribe(() => {
        this.fetchDiscovery();
      });
  }

  async fetchDiscovery() {
    if (this.formGroup.valid) {
      await this.handler.updateContext(this.formGroup.value);
    }
  }

  getPrerenderWaitUntilOptions(): LabelledSelectOption[] {
    return Object.values(GqlPuppeteerWaitUntil).map((option) => ({
      label: option.toLowerCase(),
      value: option,
    }));
  }

  closeModal() {
    return this.modalCtrl.dismiss();
  }
}
