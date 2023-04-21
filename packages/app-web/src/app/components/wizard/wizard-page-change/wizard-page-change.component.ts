import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { TypedFormControls } from '../wizard.module';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { EmbedWebsite } from '../../embedded-website/embedded-website.component';
import { WizardHandler } from '../wizard-handler';
import {
  GqlFragmentWatchFeedCreateInput,
  GqlHarvestEmitType,
} from '../../../../generated/graphql';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-wizard-page-change',
  templateUrl: './wizard-page-change.component.html',
  styleUrls: ['./wizard-page-change.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WizardPageChangeComponent implements OnInit, OnDestroy {
  @Input()
  handler: WizardHandler;

  formGroup: FormGroup<TypedFormControls<GqlFragmentWatchFeedCreateInput>>;
  embedWebsiteData: EmbedWebsite;

  private subscriptions: Subscription[] = [];

  constructor() {}

  ngOnInit() {
    this.formGroup = new FormGroup<
      TypedFormControls<GqlFragmentWatchFeedCreateInput>
    >(
      {
        compareBy: new FormControl<GqlHarvestEmitType>(
          GqlHarvestEmitType.Text,
          [Validators.required]
        ),
        fragmentXpath: new FormControl<string>('/', [Validators.required]),
      },
      { updateOn: 'change' }
    );

    this.subscriptions.push(
      this.formGroup.valueChanges.subscribe(async (value) => {
        await this.handler.updateContext({
          feed: {
            create: {
              fragmentWatchFeed: {
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
}
