import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Subscription } from 'rxjs';
import { ModalService } from '../../../services/modal.service';
import { FormControl, FormGroup } from '@angular/forms';
import { ModalController } from '@ionic/angular/standalone';
import { FeedOrRepository } from '../../../components/feed-builder/feed-builder.component';
import {
  GqlItemFilterParamsInput,
  GqlNumberFilterOperator,
  GqlSourceInput,
} from '../../../../generated/graphql';
import { ServerConfigService } from '../../../services/server-config.service';
import { addIcons } from 'ionicons';
import { closeOutline } from 'ionicons/icons';

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

  formFg = new FormGroup({
    kind: new FormControl<KindOfTracker>('dynamic'),
    fetchFrequency: new FormControl<string>('0 */15 * * * *'),
    sunset: new FormControl<SunsetPolicy>('24_hours'),
    sensitivity: new FormControl<number>(0),
    limit: new FormControl<number>(null),
  });
  private source: GqlSourceInput;

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly modalService: ModalService,
    private readonly serverConfig: ServerConfigService,
    private readonly modalCtrl: ModalController,
  ) {
    addIcons({ closeOutline });
  }

  async ngOnInit() {
    this.isThrottled = !this.serverConfig.isSelfHosted();
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
      async (data: FeedOrRepository, role: String) => {
        if (data?.feed) {
          throw new Error('not implemented');
          // this.source = createSource(
          //   data.feed.feed,
          //   data.feed.source as GqlScrapeRequest,
          // );
          // await this.updateFeed();
        }
      },
    );
  }

  closeModal() {
    return this.modalCtrl.dismiss();
  }

  private async updateFeed() {
    const filters: GqlItemFilterParamsInput[] = [];
    if (this.formFg.controls.limit.valid) {
      filters.push({
        composite: {
          exclude: {
            index: {
              operator: GqlNumberFilterOperator.Gt,
              value: this.formFg.value.limit,
            },
          },
        },
      });
    }

    this.changeRef.detectChanges();
  }

  createOrUpdatePageTracker() {}
}
