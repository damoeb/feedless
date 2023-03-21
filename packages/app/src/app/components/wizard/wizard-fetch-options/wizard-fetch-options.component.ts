import { Component, Input, OnInit } from '@angular/core';
import { FeedService } from '../../../services/feed.service';
import {
  GqlFetchOptionsInput,
  GqlPuppeteerWaitUntil,
} from '../../../../generated/graphql';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { TypedFormControls } from '../wizard.module';
import { ModalController } from '@ionic/angular';
import { WizardHandler } from '../wizard-handler';
import { LabelledSelectOption } from '../wizard-generic-feeds/wizard-generic-feeds.component';

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
        GqlFetchOptionsInput,
        'websiteUrl' | 'prerender' | 'prerenderWaitUntil' | 'prerenderScript'
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
          GqlFetchOptionsInput,
          'websiteUrl' | 'prerender' | 'prerenderWaitUntil' | 'prerenderScript'
        >
      >
    >(
      {
        websiteUrl: new FormControl(context.fetchOptions.websiteUrl, [
          Validators.required,
        ]),
        prerender: new FormControl(context.fetchOptions.prerender, [
          Validators.required,
        ]),
        prerenderScript: new FormControl(
          context.fetchOptions.prerenderScript,
          []
        ),
        prerenderWaitUntil: new FormControl(
          context.fetchOptions.prerenderWaitUntil,
          [Validators.required]
        ),
      },
      { updateOn: 'change' }
    );

    // this.formGroup.valueChanges
    //   .pipe(throttle(() => interval(1000)))
    //   .subscribe(() => {
    //     this.fetchDiscovery();
    //   });
  }

  async fetchDiscovery() {
    if (this.formGroup.valid) {
      await this.handler.updateContext({
        fetchOptions: {
          prerender: this.formGroup.value.prerender,
          prerenderScript: this.formGroup.value.prerenderScript,
          prerenderWaitUntil: this.formGroup.value.prerenderWaitUntil,
          prerenderWithoutMedia: false,
          websiteUrl: this.formGroup.value.websiteUrl,
        },
      });
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
