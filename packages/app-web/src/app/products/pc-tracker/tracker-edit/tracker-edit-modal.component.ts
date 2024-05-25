import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
  ViewChild,
} from '@angular/core';
import { Subscription } from 'rxjs';
import { ModalService } from '../../../services/modal.service';
import { FormControl, FormGroup } from '@angular/forms';
import { ModalController } from '@ionic/angular';
import { FeedWithRequest } from '../../../components/feed-builder/feed-builder.component';
import { RemoteFeedPreviewComponent } from '../../../components/remote-feed-preview/remote-feed-preview.component';
import { getScrapeRequest } from '../../../modals/generate-feed-modal/generate-feed-modal.component';
import {
  GqlCompositeFilterParamsInput,
  GqlNumberFilterOperator,
  GqlScrapeRequest,
  GqlStringFilterOperator,
} from '../../../../generated/graphql';
import { ServerSettingsService } from '../../../services/server-settings.service';

type KindOfTracker = 'static' | 'dynamic';
type SunsetPolicy = 'FirstSnapshot' | '12_hours' | '24_hours';

export interface TrackerEditModalComponentProps {}

@Component({
  selector: 'app-tracker-edit-page',
  templateUrl: './tracker-edit-modal.component.html',
  styleUrls: ['./tracker-edit-modal.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TrackerEditModalComponent
  implements TrackerEditModalComponentProps, OnInit, OnDestroy
{
  private subscriptions: Subscription[] = [];
  isThrottled: boolean;

  @ViewChild('remoteFeedPreviewComponent')
  remoteFeedPreview: RemoteFeedPreviewComponent;

  formFg = new FormGroup({
    kind: new FormControl<KindOfTracker>('dynamic'),
    fetchFrequency: new FormControl<string>('0 */15 * * * *'),
    sunset: new FormControl<SunsetPolicy>('24_hours'),
    sensitivity: new FormControl<number>(0),
    limit: new FormControl<number>(null),
  });
  private scrapeRequest: GqlScrapeRequest;

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly modalService: ModalService,
    private readonly serverSettings: ServerSettingsService,
    private readonly modalCtrl: ModalController,
  ) {}

  async ngOnInit() {
    this.isThrottled = !this.serverSettings.isSelfHosted();
    this.subscriptions.push(
      this.formFg.controls.limit.valueChanges.subscribe(async () => {
        await this.updateFeed();
      }),
    );

    this.changeRef.detectChanges();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async openFeedBuilder() {
    await this.modalService.openFeedBuilder(
      {
        modalTitle: 'Pick Link Source',
        submitButtonText: 'Use Feed',
      },
      async (data: FeedWithRequest, role: String) => {
        if (data) {
          this.scrapeRequest = getScrapeRequest(
            data.feed,
            data.scrapeRequest as GqlScrapeRequest,
          );
          await this.updateFeed();
        }
      },
    );
  }

  closeModal() {
    return this.modalCtrl.dismiss();
  }

  private async updateFeed() {
    const filters: GqlCompositeFilterParamsInput[] = [];
    if (this.formFg.controls.limit.valid) {
      filters.push({
        exclude: {
          index: {
            operator: GqlNumberFilterOperator.Gt,
            value: this.formFg.value.limit,
          },
        },
      });
    }

    await this.remoteFeedPreview.loadFeedPreview(
      [this.scrapeRequest],
      filters,
      [],
    );
    this.changeRef.detectChanges();
  }

  createOrUpdatePageTracker() {}
}
