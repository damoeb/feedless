import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnInit,
} from '@angular/core';
import { FeedService, Selectors } from '../../../services/feed.service';
import { GqlExtendContentOptions } from '../../../../generated/graphql';
import { ServerSettingsService } from '../../../services/server-settings.service';
import { TypedFormControls } from '../wizard.module';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { debounce, interval } from 'rxjs';
import { WizardHandler } from '../wizard-handler';

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
    const currentSelectors =
      this.handler.getContext().feed.create.genericFeed.specification.selectors;

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

    this.feedUrl = this.handler.getContext().feedUrl;

    this.formGroup.valueChanges
      .pipe(debounce(() => interval(500)))
      .subscribe(() => {
        if (this.formGroup.valid) {
          const genericFeed = this.handler.getContext().feed.create.genericFeed;
          genericFeed.specification.selectors = {
            paginationXPath: this.formGroup.value.paginationXPath,
            extendContext: this.formGroup.value.extendContext,
            linkXPath: this.formGroup.value.linkXPath,
            contextXPath: this.formGroup.value.contextXPath,
            dateXPath: this.formGroup.value.dateXPath,
            dateIsStartOfEvent: this.formGroup.value.dateIsStartOfEvent,
          };

          this.handler.updateContext({
            feed: {
              create: {
                genericFeed,
              },
            },
          });
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
}
