import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import { WizardContext } from '../wizard/wizard.component';
import {
  FeedDiscoveryResult,
  FeedService,
} from '../../../services/feed.service';
import { GqlPuppeteerWaitUntil } from '../../../../generated/graphql';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { TypedFormControls } from '../wizard.module';
import { LabelledSelectOption } from '../../feed-discovery-wizard/feed-discovery-wizard.component';
import { ModalController } from '@ionic/angular';
import { interval, throttle } from 'rxjs';

@Component({
  selector: 'app-wizard-fetch-options',
  templateUrl: './wizard-fetch-options.component.html',
  styleUrls: ['./wizard-fetch-options.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WizardFetchOptionsComponent implements OnInit, OnChanges {
  @Input()
  context: WizardContext;

  @Output()
  updateContext: EventEmitter<Partial<WizardContext>> = new EventEmitter<
    Partial<WizardContext>
  >();

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
  discovery: FeedDiscoveryResult;

  constructor(
    private readonly feedService: FeedService,
    private readonly modalCtrl: ModalController,
    private readonly changeRef: ChangeDetectorRef
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    console.log(changes);
    // this.changeRef.detectChanges();
  }

  ngOnInit() {
    this.discovery = this.context.discovery;
    this.formGroup = new FormGroup<
      TypedFormControls<
        Pick<
          WizardContext,
          'url' | 'prerender' | 'prerenderWaitUntil' | 'prerenderScript'
        >
      >
    >(
      {
        url: new FormControl(this.context.url, [Validators.required]),
        prerender: new FormControl(this.context.prerender, [
          Validators.required,
        ]),
        prerenderScript: new FormControl(this.context.prerenderScript, []),
        prerenderWaitUntil: new FormControl(this.context.prerenderWaitUntil, [
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
      this.busyResolvingUrl = true;
      this.changeRef.detectChanges();
      console.log('fetchDiscovery', this.formGroup.value);
      this.discovery = await this.feedService.discoverFeeds({
        fetchOptions: {
          websiteUrl: this.formGroup.value.url,
          prerender: this.formGroup.value.prerender,
          prerenderScript: this.formGroup.value.prerenderScript,
          prerenderWaitUntil: this.formGroup.value.prerenderWaitUntil,
          prerenderWithoutMedia: false,
        },
        parserOptions: {
          strictMode: false,
        },
      });
      this.busyResolvingUrl = false;
      this.changeRef.detectChanges();

      if (!this.discovery.failed) {
        this.updateContext.emit({
          discovery: this.discovery,
          ...this.formGroup.value,
        });
      }
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
