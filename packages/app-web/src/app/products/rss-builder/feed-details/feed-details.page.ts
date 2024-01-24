import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { WebDocumentService } from '../../../services/web-document.service';
import { SourceSubscription, WebDocument } from '../../../graphql/types';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { SourceSubscriptionService } from '../../../services/source-subscription.service';
import { dateFormat, dateTimeFormat } from '../../../services/profile.service';


@Component({
  selector: 'app-feed-details',
  templateUrl: './feed-details.page.html',
  styleUrls: ['./feed-details.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FeedDetailsPage implements OnInit, OnDestroy {
  busy = false;
  documents: WebDocument[];
  safeDiffImageUrl: SafeResourceUrl;
  private subscriptions: Subscription[] = [];
  private diffImageUrl: string;
  subscription: SourceSubscription;

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly activatedRoute: ActivatedRoute,
    private readonly domSanitizer: DomSanitizer,
    private readonly sourceSubscriptionService: SourceSubscriptionService,
    private readonly webDocumentService: WebDocumentService
  ) {
  }

  ngOnInit() {
    this.subscriptions.push(
      this.activatedRoute.params.subscribe(params => {
        if (params.feedId) {
          this.fetch(params.feedId);
        }
      })
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

    this.subscription = await this.sourceSubscriptionService.getSubscriptionById(id);
    this.documents = await this.webDocumentService.findAllByStreamId({
      cursor: {
        page,
        pageSize: 10
      },
      where: {
        sourceSubscription: {
          where: {
            id
          }
        }
      }
    });

    this.busy = false;
    this.changeRef.detectChanges();
  }

  protected readonly dateFormat = dateFormat;
  protected readonly dateTimeFormat = dateTimeFormat;
}
