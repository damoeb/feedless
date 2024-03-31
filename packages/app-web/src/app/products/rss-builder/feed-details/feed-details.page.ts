import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { WebDocumentService } from '../../../services/web-document.service';
import { SourceSubscription, SubscriptionSource, WebDocument } from '../../../graphql/types';
import { DomSanitizer } from '@angular/platform-browser';
import { SourceSubscriptionService } from '../../../services/source-subscription.service';
import { dateFormat, dateTimeFormat } from '../../../services/profile.service';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import { ServerSettingsService } from '../../../services/server-settings.service';
import { ModalService } from '../../../services/modal.service';
import { FeedWithRequest } from '../../../components/feed-builder/feed-builder.component';
import { GqlScrapeRequest } from '../../../../generated/graphql';
import { GenerateFeedModalComponentProps, getScrapeRequest } from '../../../modals/generate-feed-modal/generate-feed-modal.component';
import { ModalController } from '@ionic/angular';

@Component({
  selector: 'app-feed-details-page',
  templateUrl: './feed-details.page.html',
  styleUrls: ['./feed-details.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeedDetailsPage implements OnInit, OnDestroy {
  busy = false;
  documents: WebDocument[];
  private subscriptions: Subscription[] = [];
  private diffImageUrl: string;
  subscription: SourceSubscription;

  protected readonly dateFormat = dateFormat;
  protected readonly dateTimeFormat = dateTimeFormat;
  feedUrl: string;
  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly activatedRoute: ActivatedRoute,
    private readonly modalCtrl: ModalController,
    private readonly modalService: ModalService,
    private readonly serverSettingsService: ServerSettingsService,
    private readonly domSanitizer: DomSanitizer,
    private readonly sourceSubscriptionService: SourceSubscriptionService,
    private readonly webDocumentService: WebDocumentService,
  ) {}

  ngOnInit() {
    dayjs.extend(relativeTime);
    this.subscriptions.push(
      this.activatedRoute.params.subscribe((params) => {
        if (params.feedId) {
          this.fetch(params.feedId);
        }
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
    URL.revokeObjectURL(this.diffImageUrl);
  }

  private async fetch(id: string) {
    const page = 0;
    this.busy = true;
    this.changeRef.detectChanges();

    this.subscription =
      await this.sourceSubscriptionService.getSubscriptionById(id);
    this.feedUrl = `${this.serverSettingsService.gatewayUrl}/feed/${this.subscription.id}`;
    this.documents = await this.webDocumentService.findAllByStreamId({
      cursor: {
        page,
        pageSize: 10,
      },
      where: {
        sourceSubscription: {
          where: {
            id,
          },
        },
      },
    });

    this.busy = false;
    this.changeRef.detectChanges();
  }
  fromNow(futureTimestamp: number): string {
    return dayjs(futureTimestamp).toNow(true);
  }

  deleteWebDocument(document: WebDocument) {
    return this.webDocumentService.removeById({
      where: {
        id: document.id,
      },
    });
  }

  async editSource(source: SubscriptionSource = null) {
    await this.modalService.openFeedBuilder({
      scrapeRequest: source as any
    }, async (data: FeedWithRequest) => {
      if (data) {
        await this.sourceSubscriptionService.updateSubscription({
          where: {
            id: this.subscription.id
          },
          data: {
            sources: {
              add: [getScrapeRequest(data.feed, data.scrapeRequest as GqlScrapeRequest)],
              remove: source ? [source.id] : []
            }
          }
        });
      }
    });
  }

  deleteSource(source: SubscriptionSource) {
    console.log('deleteSource', source);
    return this.sourceSubscriptionService.updateSubscription({
      where: {
        id: this.subscription.id
      },
      data: {
        sources: {
          remove: [source.id]
        }
      }
    })
  }

  dismissModal() {
    this.modalCtrl.dismiss()
  }

  hostname(url: string): string {
    return new URL(url).hostname
  }

  async editSubscription() {
    const componentProps: GenerateFeedModalComponentProps = {
      subscription: this.subscription
    };
    await this.modalService.openFeedMetaEditor(componentProps);
  }

  deleteSubscription() {
    return this.sourceSubscriptionService.deleteSubscription({
      id: this.subscription.id
    })
  }

  getRetentionStrategy(): string {
    if (this.subscription.retention.maxAgeDays || this.subscription.retention.maxItems) {
      if (this.subscription.retention.maxAgeDays && this.subscription.retention.maxItems) {
        return `${this.subscription.retention.maxAgeDays} days, ${this.subscription.retention.maxItems} items`;
      } else {
        if (this.subscription.retention.maxAgeDays) {
          return `${this.subscription.retention.maxAgeDays} days`;
        } else {
          return `${this.subscription.retention.maxItems} items`;
        }
      }
    } else {
      return 'Auto';
    }
  }
}
