import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit } from '@angular/core';
import { FeedService, Selectors } from '../../../services/feed.service';
import { GqlExtendContentOptions } from '../../../../generated/graphql';
import { LabelledSelectOption } from '../../feed-discovery-wizard/feed-discovery-wizard.component';
import { webToFeedParams } from '../../api-params';
import { ServerSettingsService } from '../../../services/server-settings.service';
import { TypedFormControls } from '../wizard.module';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { interval, throttle } from 'rxjs';
import { WizardHandler } from '../wizard-handler';

@Component({
  selector: 'app-wizard-generic-feeds',
  templateUrl: './wizard-generic-feeds.component.html',
  styleUrls: ['./wizard-generic-feeds.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WizardGenericFeedsComponent implements OnInit {
  @Input()
  handler: WizardHandler;

  feedUrl: string;
  formGroup: FormGroup<TypedFormControls<Selectors>>;

  constructor(
    private readonly feedService: FeedService,
    private readonly serverSettingsService: ServerSettingsService,
    private readonly changeRef: ChangeDetectorRef
  ) {}

  async ngOnInit() {
    const currentSelectors = this.handler.getContext().genericFeed?.selectors;

    this.formGroup = new FormGroup<TypedFormControls<Selectors>>(
      {
        contextXPath: new FormControl(currentSelectors?.contextXPath, [
          Validators.required,
        ]),
        dateXPath: new FormControl(currentSelectors?.dateXPath, []),
        linkXPath: new FormControl(currentSelectors?.linkXPath, [
          Validators.required,
        ]),
        dateIsStartOfEvent: new FormControl(
          currentSelectors?.dateIsStartOfEvent,
          [Validators.required]
        ),
        extendContext: new FormControl(currentSelectors?.extendContext, []),
        paginationXPath: new FormControl(currentSelectors?.paginationXPath, []),
      },
      { updateOn: 'change' }
    );

    if (this.handler.getContext().genericFeed) {
      this.feedUrl = this.handler.getContext().genericFeed.feedUrl;
      await this.handler.updateContext({ feedUrl: this.feedUrl });
    }

    this.formGroup.valueChanges
      .pipe(throttle(() => interval(1000)))
      .subscribe(() => {
        console.log('update');
        if (this.formGroup.valid) {
          this.feedUrl = this.getCurrentFeedUrl();
          this.handler.updateContext({ feedUrl: this.feedUrl });
          this.changeRef.detectChanges();
        } else {
          console.log('errornous');
          this.handler.updateContext({ feedUrl: '' });
        }
      });
  }

  getExtendContextOptions(): LabelledSelectOption[] {
    return Object.values(GqlExtendContentOptions).map((option) => ({
      label: option,
      value: option,
    }));
  }

  private getCurrentFeedUrl(): string {
    const str = (value: boolean | number): string => `${value}`;

    const selectors = this.formGroup.value;
    const searchParams = new URLSearchParams();
    searchParams.set(webToFeedParams.version, '0.1');
    searchParams.set(
      webToFeedParams.url,
      this.handler.getDiscovery().websiteUrl
    );
    searchParams.set(webToFeedParams.contextPath, selectors.contextXPath);
    searchParams.set(webToFeedParams.paginationPath, selectors.paginationXPath);
    searchParams.set(webToFeedParams.datePath, selectors.dateXPath);
    searchParams.set(webToFeedParams.linkPath, selectors.linkXPath);
    searchParams.set(
      webToFeedParams.eventFeed,
      str(selectors.dateIsStartOfEvent)
    );
    searchParams.set(
      webToFeedParams.extendContent,
      this.toExtendContextParam(selectors.extendContext)
    );
    searchParams.set(
      webToFeedParams.prerender,
      str(this.handler.getContext().prerender)
    );
    searchParams.set(webToFeedParams.strictMode, str(false));
    searchParams.set(
      webToFeedParams.prerenderWaitUntil,
      this.handler.getContext().prerenderWaitUntil
    );

    return (
      this.serverSettingsService.getApiUrls().webToFeed +
      '?' +
      searchParams.toString()
    );
  }

  private toExtendContextParam(extendContext: GqlExtendContentOptions): string {
    switch (extendContext) {
      case GqlExtendContentOptions.PreviousAndNext:
        return 'pn';
      default:
        return extendContext.toString()[0].toLowerCase();
    }
  }
}
