import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Subscription } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { WebDocumentService } from '../../../services/web-document.service';
import {
  FeedlessPlugin,
  SourceSubscription,
  SubscriptionSource,
  WebDocument,
} from '../../../graphql/types';
import { DomSanitizer } from '@angular/platform-browser';
import { SourceSubscriptionService } from '../../../services/source-subscription.service';
import { dateFormat, dateTimeFormat } from '../../../services/session.service';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import { ServerSettingsService } from '../../../services/server-settings.service';
import { ModalService } from '../../../services/modal.service';
import { FeedWithRequest } from '../../../components/feed-builder/feed-builder.component';
import { GqlScrapeRequest } from '../../../../generated/graphql';
import {
  GenerateFeedModalComponentProps,
  getScrapeRequest,
} from '../../../modals/generate-feed-modal/generate-feed-modal.component';
import { ModalController } from '@ionic/angular';
import { BubbleColor } from '../../../components/bubble/bubble.component';
import { ArrayElement } from '../../../types';
import { PluginService } from '../../../services/plugin.service';

@Component({
  selector: 'app-tracker-details-page',
  templateUrl: './tracker-details.page.html',
  styleUrls: ['./tracker-details.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TrackerDetailsPage implements OnInit, OnDestroy {
  busy = false;
  documents: WebDocument[];
  private subscriptions: Subscription[] = [];
  private diffImageUrl: string;
  subscription: SourceSubscription;

  feedUrl: string;
  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly activatedRoute: ActivatedRoute,
    private readonly modalCtrl: ModalController,
  ) {}

  async ngOnInit() {
    dayjs.extend(relativeTime);
    this.subscriptions.push(
      this.activatedRoute.params.subscribe((params) => {
        if (params.trackerId) {
        }
      }),
    );
    this.changeRef.detectChanges();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
    URL.revokeObjectURL(this.diffImageUrl);
  }

  dismissModal() {
    this.modalCtrl.dismiss();
  }
}
