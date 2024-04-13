import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Subscription } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { SourceSubscription, WebDocument } from '../../../graphql/types';
import { SourceSubscriptionService } from '../../../services/source-subscription.service';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import { BubbleColor } from '../../../components/bubble/bubble.component';

@Component({
  selector: 'app-feeds-page',
  templateUrl: './feeds.page.html',
  styleUrls: ['./feeds.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeedsPage implements OnInit, OnDestroy {
  busy = false;
  documents: WebDocument[];
  private subscriptions: Subscription[] = [];
  feeds: SourceSubscription[] = [];

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly activatedRoute: ActivatedRoute,
    private readonly sourceSubscriptionService: SourceSubscriptionService,
  ) {}

  async ngOnInit() {
    dayjs.extend(relativeTime);
    //     this.subscriptions.push(
    // ,
    //     );
    await this.fetchFeeds();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  private async fetchFeeds() {
    const page = 0;
    const sources =
      await this.sourceSubscriptionService.listSourceSubscriptions({
        cursor: {
          page,
        },
      });
    this.feeds.push(...sources);
    this.changeRef.detectChanges();
  }

  getHealthColorForFeed(feed: SourceSubscription): BubbleColor {
    if (feed.sources.some((source) => source.errornous)) {
      return 'red';
    } else {
      return 'blue';
    }
  }
}
