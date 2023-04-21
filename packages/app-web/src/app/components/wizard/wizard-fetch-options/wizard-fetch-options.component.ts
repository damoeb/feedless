import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnInit,
} from '@angular/core';
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
import { isUndefined, pick } from 'lodash-es';
import { fixUrl } from '../../../pages/getting-started/getting-started.page';

const defaultFetchOptions: GqlFetchOptionsInput = {
  prerender: false,
  websiteUrl: '',
  prerenderWaitUntil: GqlPuppeteerWaitUntil.Load,
  prerenderScript: '',
  prerenderWithoutMedia: false,
};

type FormFetchOptions = Pick<
  GqlFetchOptionsInput,
  'websiteUrl' | 'prerender' | 'prerenderWaitUntil' | 'prerenderScript'
>;

@Component({
  selector: 'app-wizard-fetch-options',
  templateUrl: './wizard-fetch-options.component.html',
  styleUrls: ['./wizard-fetch-options.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WizardFetchOptionsComponent implements OnInit {
  @Input()
  handler: WizardHandler;

  @Input()
  options = true;

  formGroup: FormGroup<TypedFormControls<FormFetchOptions>>;

  busyResolvingUrl = false;

  constructor(
    private readonly feedService: FeedService,
    private readonly changeRef: ChangeDetectorRef,
    private readonly modalCtrl: ModalController
  ) {}

  ngOnInit() {
    const context = this.handler.getContext();
    this.formGroup = new FormGroup<TypedFormControls<FormFetchOptions>>(
      {
        websiteUrl: new FormControl(defaultFetchOptions.websiteUrl, [
          Validators.required,
          Validators.minLength(4),
        ]),
        prerender: new FormControl(defaultFetchOptions.prerender, [
          Validators.required,
        ]),
        prerenderScript: new FormControl(
          defaultFetchOptions.prerenderScript,
          []
        ),
        prerenderWaitUntil: new FormControl(
          defaultFetchOptions.prerenderWaitUntil,
          [Validators.required]
        ),
      },
      { updateOn: 'submit' }
    );
    this.formGroup.setValue(
      pick(
        context.fetchOptions,
        'websiteUrl',
        'prerender',
        'prerenderScript',
        'prerenderWaitUntil'
      )
    );
    this.changeRef.detectChanges();

    this.formGroup.valueChanges.subscribe(async () => {
      if (this.formGroup.valid) {
        await this.handler.updateContext({
          isCurrentStepValid: true,
          fetchOptions: {
            prerender: this.formGroup.value.prerender,
            prerenderScript: this.formGroup.value.prerenderScript,
            prerenderWaitUntil: this.formGroup.value.prerenderWaitUntil,
            prerenderWithoutMedia: false,
            websiteUrl: fixUrl(this.formGroup.value.websiteUrl),
          },
        });
      }
    });

    this.handler.onContextChange().subscribe((change) => {
      if (!isUndefined(change.busy)) {
        this.busyResolvingUrl = change.busy;
        this.changeRef.detectChanges();
      }
    });
  }

  async fetchDiscovery(url: string) {
    this.formGroup.controls.websiteUrl.setValue(url);
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
