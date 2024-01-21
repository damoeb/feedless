import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { WebDocumentService } from '../../../services/web-document.service';
import { WebDocument } from '../../../graphql/types';

@Component({
  selector: 'app-visual-diff-details',
  templateUrl: './subscription-details.page.html',
  styleUrls: ['./subscription-details.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SubscriptionDetailsPage implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];

  busy = false;
  documents: WebDocument[];

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly activatedRoute: ActivatedRoute,
    private readonly webDocumentService: WebDocumentService,
  ) {
  }

  ngOnInit() {
    this.subscriptions.push(
      this.activatedRoute.params.subscribe(params => {
        if (params.id) {
          this.fetch(params.id)
        }
      })
    );

    this.changeRef.detectChanges();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  private async fetch(id: string) {
    const page = 0;
    this.documents = await this.webDocumentService.findAllByStreamId({
      cursor: {
        page,
        pageSize: 2
      },
      where: {
        sourceSubscription: {
          where: {
            id
          }
        }
      }
    });
    this.changeRef.detectChanges();
  }
}
