import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnInit,
} from '@angular/core';
import { ModalController } from '@ionic/angular';
import {
  ItemsFilterModalComponent,
  ItemsFilterModalComponentProps,
} from '../../../modals/items-filter-modal/items-filter-modal.component';
import { WizardHandler } from '../wizard-handler';
import {
  GqlImportersCreateInput,
  GqlNativeFeedCreateInput,
} from '../../../../generated/graphql';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { TypedFormControls } from '../wizard.module';
import { debounce, interval } from 'rxjs';
import { FeedService } from '../../../services/feed.service';
import { WizardStepId } from '../wizard/wizard.component';

type ImporterFormData = Pick<
  GqlImportersCreateInput,
  'email' | 'filter' | 'webhook' | 'autoRelease'
>;

type NativeFeedFormData = Pick<GqlNativeFeedCreateInput, 'harvestIntervalMin'>;

@Component({
  selector: 'app-wizard-importer',
  templateUrl: './wizard-importer.component.html',
  styleUrls: ['./wizard-importer.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WizardImporterComponent implements OnInit {
  @Input()
  handler: WizardHandler;

  feedUrl: string;

  importerFormGroup: FormGroup<TypedFormControls<ImporterFormData>>;
  nativeFeedFormGroup: FormGroup<TypedFormControls<NativeFeedFormData>>;
  internalFormGroup: FormGroup<{ showFilter: FormControl<boolean> }>;
  feed: { color: string; text: string; title: string };

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly changeRef: ChangeDetectorRef,
    private readonly feedService: FeedService
  ) {}

  ngOnInit() {
    const context = this.handler.getContext();
    this.feedUrl = context.feedUrl;
    this.importerFormGroup = new FormGroup<TypedFormControls<ImporterFormData>>(
      {
        email: new FormControl<string>(context.importer?.email),
        filter: new FormControl<string>(context.importer?.filter || ''),
        webhook: new FormControl<string>(context.importer?.webhook),
        autoRelease: new FormControl<boolean>(
          context.importer?.autoRelease || true
        ),
      },
      { updateOn: 'change' }
    );

    this.importerFormGroup.valueChanges
      .pipe(debounce(() => interval(500)))
      .subscribe(() => this.tryEmitImporter());

    this.tryEmitImporter();

    this.nativeFeedFormGroup = new FormGroup<
      TypedFormControls<NativeFeedFormData>
    >(
      {
        harvestIntervalMin: new FormControl<number>(10, [
          Validators.min(2),
          Validators.max(30000),
        ]),
      },
      { updateOn: 'change' }
    );

    this.nativeFeedFormGroup.valueChanges
      .pipe(debounce(() => interval(500)))
      .subscribe(async () => {
        await this.handler.updateContext({
          // todo NativeFeedUpdateInput
        });
      });

    this.internalFormGroup = new FormGroup(
      {
        showFilter: new FormControl<boolean>(
          context.importer?.filter?.length > 0
        ),
      },
      { updateOn: 'change' }
    );

    this.fetchFeed(context.feedUrl);
  }

  async showFilterModal() {
    const componentProps: ItemsFilterModalComponentProps = {
      filterExpression: this.importerFormGroup.value.filter,
    };
    const modal = await this.modalCtrl.create({
      component: ItemsFilterModalComponent,
      componentProps,
      backdropDismiss: false,
    });
    await modal.present();
    const response = await modal.onDidDismiss();
    switch (response.role) {
      case 'persist':
        this.importerFormGroup.controls.filter.setValue(response.data);
        break;
      case 'clear':
        this.importerFormGroup.controls.filter.setValue('');
        break;
    }
  }

  goToSources() {
    return this.handler.updateContext({
      stepId: WizardStepId.source,
      history: [],
    });
  }

  humanizeMinutes(): string {
    const minutes = this.nativeFeedFormGroup.value.harvestIntervalMin;
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

  private async tryEmitImporter() {
    if (this.importerFormGroup.valid) {
      await this.handler.updateContext({
        importer: {
          autoRelease: this.importerFormGroup.value.autoRelease,
          filter: this.importerFormGroup.value.filter,
          webhook: this.importerFormGroup.value.webhook,
          email: this.importerFormGroup.value.email,
        },
      });
    }
  }

  private async fetchFeed(feedUrl: string) {
    try {
      this.feed = await this.feedService
        .remoteFeed(feedUrl)
        .then((remoteFeed) => ({
          title: remoteFeed.title,
          text: remoteFeed.feedUrl,
          color: 'medium',
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
