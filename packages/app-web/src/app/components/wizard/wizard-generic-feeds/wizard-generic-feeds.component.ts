import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { FeedService } from '../../../services/feed.service';
import { GqlExtendContentOptions } from '../../../../generated/graphql';
import { ServerSettingsService } from '../../../services/server-settings.service';
import { TypedFormControls } from '../wizard.module';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { debounce, interval, Subscription } from 'rxjs';
import { WizardHandler } from '../wizard-handler';
import { EmbedWebsite } from '../../embedded-website/embedded-website.component';
import { Selectors } from '../../../graphql/types';

export interface LabelledSelectOption {
  value: string;
  label: string;
}

@Component({
  selector: 'app-wizard-generic-feeds',
  templateUrl: './wizard-generic-feeds.component.html',
  styleUrls: ['./wizard-generic-feeds.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WizardGenericFeedsComponent implements OnInit, OnDestroy {
  @Input()
  handler: WizardHandler;

  feedUrl: string;
  formGroup: FormGroup<TypedFormControls<Selectors>>;
  embedWebsiteData: EmbedWebsite;
  segmentFeed = 'feed';

  private subscriptions: Subscription[] = [];

  constructor(
    private readonly feedService: FeedService,
    private readonly serverSettingsService: ServerSettingsService,
    private readonly changeRef: ChangeDetectorRef
  ) {}

  async ngOnInit() {
    const currentSelectors =
      this.handler.getContext().feed.create.genericFeed.specification.selectors;

    const discovery = this.handler.getDiscovery();
    if (discovery && this.embedWebsiteData?.url !== discovery.websiteUrl) {
      this.embedWebsiteData = {
        htmlBody: discovery.document.htmlBody,
        mimeType: discovery.document.mimeType,
        url: discovery.websiteUrl,
      };
    }

    this.formGroup = new FormGroup<TypedFormControls<Selectors>>(
      {
        contextXPath: new FormControl('', [Validators.required]),
        dateXPath: new FormControl('', []),
        linkXPath: new FormControl('', [Validators.required]),
        dateIsStartOfEvent: new FormControl(false, [Validators.required]),
        extendContext: new FormControl(GqlExtendContentOptions.None, []),
        paginationXPath: new FormControl('', []),
      },
      { updateOn: 'change' }
    );

    this.formGroup.setValue({
      contextXPath: currentSelectors?.contextXPath,
      dateIsStartOfEvent: currentSelectors?.dateIsStartOfEvent,
      dateXPath: currentSelectors?.dateXPath,
      linkXPath: currentSelectors?.linkXPath,
      extendContext: currentSelectors?.extendContext,
      paginationXPath: currentSelectors?.paginationXPath,
    });

    this.feedUrl = this.handler.getContext().feedUrl;

    this.subscriptions.push(
      this.formGroup.valueChanges
        .pipe(debounce(() => interval(500)))
        .subscribe(() => {
          if (this.formGroup.valid) {
            const genericFeed =
              this.handler.getContext().feed.create.genericFeed;
            genericFeed.specification.selectors = {
              paginationXPath: this.formGroup.value.paginationXPath,
              extendContext: this.formGroup.value.extendContext,
              linkXPath: this.formGroup.value.linkXPath,
              contextXPath: this.formGroup.value.contextXPath,
              dateXPath: this.formGroup.value.dateXPath,
              dateIsStartOfEvent: this.formGroup.value.dateIsStartOfEvent,
            };

            console.log('genericFeed', genericFeed);
            this.handler.updateContext({
              isCurrentStepValid: true,
              feed: {
                create: {
                  genericFeed,
                },
              },
            });
            this.changeRef.detectChanges();
          } else {
            console.log('errornous');
            this.handler.updateContext({
              isCurrentStepValid: false,
              feedUrl: '',
            });
          }
        })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  getExtendContextOptions(): LabelledSelectOption[] {
    return Object.values(GqlExtendContentOptions).map((option) => ({
      label: option,
      value: option,
    }));
  }
}
