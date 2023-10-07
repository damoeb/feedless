import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { FeedService } from '../../../services/feed.service';
import { GqlPuppeteerWaitUntil } from '../../../../generated/graphql';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { TypedFormControls } from '../wizard.module';
import { ModalController } from '@ionic/angular';
import { WizardHandler } from '../wizard-handler';
import { LabelledSelectOption } from '../wizard-generic-feeds/wizard-generic-feeds.component';
import { isUndefined, pick } from 'lodash-es';
import { fixUrl } from '../../../pages/getting-started/getting-started.page';
import { Subscription } from 'rxjs';
import { ImportModalComponent } from '../../../modals/import-modal/import-modal.component';
import { WizardExitRole } from '../wizard/wizard.component';
import { FetchOptions } from '../../../graphql/types';

const defaultFetchOptions: FetchOptions = {
  prerender: false,
  websiteUrl: '',
  prerenderWaitUntil: GqlPuppeteerWaitUntil.Load,
};

type FormFetchOptions = Pick<
  FetchOptions,
  'websiteUrl' | 'prerender' | 'prerenderWaitUntil'
>;

@Component({
  selector: 'app-wizard-fetch-options',
  templateUrl: './wizard-fetch-options.component.html',
  styleUrls: ['./wizard-fetch-options.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WizardFetchOptionsComponent implements OnInit, OnDestroy {
  @Input()
  handler: WizardHandler;

  @Input()
  options = true;

  @Input()
  moreOptions = false;

  formGroup: FormGroup<TypedFormControls<FormFetchOptions>>;

  busyResolvingUrl = false;

  private subscriptions: Subscription[] = [];

  constructor(
    private readonly feedService: FeedService,
    private readonly changeRef: ChangeDetectorRef,
    private readonly modalCtrl: ModalController,
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
        prerenderWaitUntil: new FormControl(
          defaultFetchOptions.prerenderWaitUntil,
          [Validators.required],
        ),
      },
      { updateOn: 'change' },
    );
    this.formGroup.setValue(
      pick(
        context.fetchOptions,
        'websiteUrl',
        'prerender',
        'prerenderWaitUntil',
      ),
    );
    this.changeRef.detectChanges();

    this.subscriptions.push(
      this.formGroup.valueChanges.subscribe(async () => {
        if (this.formGroup.valid) {
          await this.handler.updateContext({
            isCurrentStepValid: true,
            fetchOptions: {
              prerender: this.formGroup.value.prerender,
              prerenderWaitUntil: this.formGroup.value.prerenderWaitUntil,
              websiteUrl: fixUrl(this.formGroup.value.websiteUrl),
            },
          });
        }
      }),
    );

    this.handler.onContextChange().subscribe((change) => {
      if (change && !isUndefined(change.busy)) {
        this.busyResolvingUrl = change.busy;
        this.changeRef.detectChanges();
      }
    });
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async fetchDiscovery(url: string | number) {
    this.formGroup.controls.websiteUrl.setValue(`${url}`);
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

  async showImportModal() {
    await this.modalCtrl.dismiss(undefined, WizardExitRole.dismiss);
    const modal = await this.modalCtrl.create({
      component: ImportModalComponent,
      showBackdrop: true,
    });
    await modal.present();
  }
}
