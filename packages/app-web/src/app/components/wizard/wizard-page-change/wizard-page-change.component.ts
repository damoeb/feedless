import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { TypedFormControls } from '../wizard.module';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { EmbedWebsite } from '../../embedded-website/embedded-website.component';
import { WizardContextChange, WizardHandler } from '../wizard-handler';
import { GqlFetchOptions, GqlFragmentWatchFeedCreateInput, GqlHarvestEmitType } from '../../../../generated/graphql';
import { Subscription } from 'rxjs';
import { clone, isEqual, isUndefined } from 'lodash-es';

type FormValues = Pick<
  GqlFragmentWatchFeedCreateInput,
  'title' | 'compareBy' | 'fragmentXpath'
> & { refreshRateMin: number };

@Component({
  selector: 'app-wizard-page-change',
  templateUrl: './wizard-page-change.component.html',
  styleUrls: ['./wizard-page-change.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WizardPageChangeComponent implements OnInit, OnDestroy {
  @Input()
  handler: WizardHandler;

  formGroup: FormGroup<TypedFormControls<FormValues>>;
  embedWebsiteData: EmbedWebsite;
  readonly refreshRateMin = 2;
  readonly refreshRateMax = 7 * 24 * 60;

  private subscriptions: Subscription[] = [];
  private currentFetchOptions: Pick<
    GqlFetchOptions,
    'prerender' | 'websiteUrl' | 'prerenderScript' | 'prerenderWaitUntil'
  >;

  constructor(private readonly changeRef: ChangeDetectorRef) {}

  ngOnInit() {
    this.formGroup = new FormGroup<TypedFormControls<FormValues>>(
      {
        compareBy: new FormControl<GqlHarvestEmitType>(
          GqlHarvestEmitType.Text,
          [Validators.required]
        ),
        fragmentXpath: new FormControl<string>('/', [Validators.required]),
        refreshRateMin: new FormControl<number>(24 * 60, [Validators.required]),
        title: new FormControl<string>(
          `Watch changes on '${this.handler.getDiscovery().document.title}'`,
          [Validators.required]
        ),
      },
      { updateOn: 'change' }
    );

    this.handler.onContextChange().subscribe((change) => {
      if (change) {
        this.handleChange(change);
      }
    });

    this.subscriptions.push(
      this.formGroup.valueChanges.subscribe(async (value) => {
        await this.handler.updateContext({
          isCurrentStepValid: this.formGroup.valid,
          feed: {
            create: {
              fragmentWatchFeed: {
                fetchOptions: this.handler.getContext().fetchOptions,
                refreshRate: {
                  scheduled: {
                    expression: `${value.refreshRateMin} min`,
                  },
                },
                title: value.title,
                fragmentXpath: value.fragmentXpath,
                compareBy: value.compareBy,
              },
            },
          },
        });
      })
    );

    const discovery = this.handler.getDiscovery();
    this.embedWebsiteData = {
      htmlBody: discovery.document.htmlBody,
      mimeType: discovery.document.mimeType,
      url: discovery.websiteUrl,
    };
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  private handleChange(change: WizardContextChange) {
    if (!isUndefined(change.busy)) {
      // this.busy = change.busy;
      this.changeRef.detectChanges();
    }
    const discovery = this.handler.getDiscovery();
    if (
      discovery &&
      !isEqual(this.currentFetchOptions, discovery.fetchOptions)
    ) {
      try {
        this.currentFetchOptions = clone(discovery.fetchOptions);
        this.embedWebsiteData = {
          htmlBody: discovery.document.htmlBody,
          mimeType: discovery.document.mimeType,
          url: discovery.websiteUrl,
        };
        this.changeRef.detectChanges();
      } catch (e) {
        console.error(e);
      }
    }
  }

  humanizeMinutes(): string {
    const minutes = this.formGroup.value.refreshRateMin;
    if (minutes < 60) {
      return minutes.toFixed(0) + 'min';
    }
    const hours = minutes / 60;
    if (hours < 24) {
      return hours.toFixed(0) + 'h';
    }
    const days = hours / 24;
    if (days < 7) {
      return days.toFixed(0) + 'd';
    }
    const weeks = days / 7;
    if (weeks < 4) {
      return days.toFixed(0) + ' w';
    }
    return (weeks / 4).toFixed(0) + ' month';
  }

  handleXpathChange(xpath: string) {
    this.formGroup.controls.fragmentXpath.setValue(xpath);
    this.changeRef.detectChanges();
  }

  getHighlightXpath(): string {
    return this.formGroup.value.fragmentXpath;
  }
}
