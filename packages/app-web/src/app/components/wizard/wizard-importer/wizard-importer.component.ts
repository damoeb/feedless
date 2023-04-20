import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnInit,
} from '@angular/core';
import { ModalController } from '@ionic/angular';
import { WizardHandler } from '../wizard-handler';
import {
  GqlFeatureName,
  GqlImportersCreateInput,
  GqlSegmentInput,
} from '../../../../generated/graphql';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { debounce, interval } from 'rxjs';
import { FeedService } from '../../../services/feed.service';
import { WizardStepId } from '../wizard/wizard.component';
import { enumToKeyValue } from '../../../pages/feeds/feeds.page';
import { FilterOption } from '../../filter-toolbar/filter-toolbar.component';

type ImporterFormData = Pick<GqlImportersCreateInput, 'filter' | 'autoRelease'>;

type SegmentFormData = Pick<
  GqlSegmentInput,
  'digest' | 'size' | 'scheduleExpression' | 'sortBy' | 'sortAsc'
>;

// interface ImporterPlugin {
//   id: string;
//   properties: {
//     enabled: boolean;
//   };
// }

// const plugins: ImporterPlugin[] = [
//   {
//     id: 'rich.fulltext',
//     properties: {
//       enabled: true
//     }
//   },
//   {
//     id: 'rich.noUrlShortener',
//     properties: {
//       enabled: true
//     }
//   },
//   {
//     id: 'rich.inlineImages',
//     properties: {
//       enabled: true
//     }
//   }
// ];

const defaultImporterFormValues: ImporterFormData & SegmentFormData = {
  autoRelease: true,
  filter: '',
  // segment
  digest: true,
  size: 5,
  scheduleExpression: '',
  sortBy: 'score',
  sortAsc: false,
};

enum HarvestRate {
  default = 'default',
  slower = 'slower',
  faster = 'faster',
}

enum ThrottlePeriod {
  day = 'Day',
  week = 'Week',
  month = 'month',
  year = 'Year',
}

@Component({
  selector: 'app-wizard-importer',
  templateUrl: './wizard-importer.component.html',
  styleUrls: ['./wizard-importer.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WizardImporterComponent implements OnInit {
  @Input()
  handler: WizardHandler;
  @Input()
  feedPreview = true;

  feedUrl: string;

  importerFormGroup: FormGroup<{
    filter: FormControl<string | null>;
    refreshRate: FormControl<number | null>;
    segmentSize: FormControl<number | null>;
    digest: FormControl<boolean | null>;
    reviewItems: FormControl<boolean | null>;
  }>;
  internalFormGroup: FormGroup<{
    showFilter: FormControl<boolean | null>;
    harvestRate: FormControl<HarvestRate | null>;
    throttlePeriod: FormControl<ThrottlePeriod | null>;
    throttleItems: FormControl<number | null>;
  }>;
  feed: { color: string; text: string; title: string };
  readonly refreshRateMin = 2;
  readonly refreshRateMax = 3000;
  harvestRateEnum = HarvestRate;
  throttlePeriodEnum = ThrottlePeriod;
  featureNameEnum = GqlFeatureName;

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly changeRef: ChangeDetectorRef,
    private readonly feedService: FeedService
  ) {}

  ngOnInit() {
    const context = this.handler.getContext();
    this.feedUrl = context.feedUrl;
    this.importerFormGroup = new FormGroup(
      {
        filter: new FormControl<string>(defaultImporterFormValues.filter || ''),
        reviewItems: new FormControl<boolean>(
          !defaultImporterFormValues.autoRelease
        ),
        digest: new FormControl<boolean>(defaultImporterFormValues.digest),
        segmentSize: new FormControl<number>(defaultImporterFormValues.size),
        refreshRate: new FormControl<number>(10, [
          Validators.min(this.refreshRateMin),
          Validators.max(this.refreshRateMax),
        ]),
      },
      { updateOn: 'change' }
    );

    if (context.importer) {
      this.importerFormGroup.setValue({
        filter: context.importer.filter,
        reviewItems: !context.importer.autoRelease,
        digest: false,
        refreshRate: 10,
        segmentSize: 5,
        // refreshRate: context.importer.refreshRate ? context.importer.refreshRate.scheduled.expression : null,
      });
    }

    this.importerFormGroup.valueChanges
      .pipe(debounce(() => interval(500)))
      .subscribe(() => this.tryEmitImporter());

    this.tryEmitImporter();

    this.internalFormGroup = new FormGroup(
      {
        showFilter: new FormControl<boolean>(
          context.importer?.filter?.length > 0
        ),
        harvestRate: new FormControl<HarvestRate>(HarvestRate.default),
        throttleItems: new FormControl<number>(5, [
          Validators.min(1),
          Validators.max(50),
        ]),
        throttlePeriod: new FormControl<ThrottlePeriod>(ThrottlePeriod.week),
      },
      { updateOn: 'change' }
    );
    this.internalFormGroup.controls.showFilter.valueChanges.subscribe(
      (checked) => {
        if (!checked) {
          this.importerFormGroup.controls.filter.setValue('');
        }
      }
    );

    if (this.feedPreview) {
      this.fetchFeed(context.feedUrl);
    }
  }

  goToSources() {
    return this.handler.updateContext({
      stepId: WizardStepId.source,
    });
  }

  humanizeMinutes(): string {
    const minutes = this.importerFormGroup.value.refreshRate;
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

  toArray(obj: object): FilterOption[] {
    return enumToKeyValue(obj);
  }

  toHarvestRateLabel(key: string): string {
    switch (key) {
      case HarvestRate.slower:
        return 'Schedule & Throttle';
      case HarvestRate.default:
        return 'Default';
      case HarvestRate.faster:
        return 'Real Time';
    }
  }

  private async tryEmitImporter() {
    await this.handler.updateContext({
      isCurrentStepValid: this.importerFormGroup.valid,
      importer: {
        autoRelease: !this.importerFormGroup.value.reviewItems,
        filter: this.importerFormGroup.value.filter,
      },
    });
  }

  private async fetchFeed(nativeFeedUrl: string) {
    try {
      this.feed = await this.feedService
        .remoteFeed({
          nativeFeedUrl,
        })
        .then((remoteFeed) => ({
          title: remoteFeed.title,
          text: remoteFeed.feedUrl,
          color: 'dark',
        }));
    } catch (e) {
      this.feed = {
        title: 'Fix the Feed URL',
        text: 'Feed cannot be fetched',
        color: 'danger',
      };
    }
    this.changeRef.detectChanges();
  }
}
