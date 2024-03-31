import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { SourceSubscriptionService } from '../../../services/source-subscription.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-visual-diff-list-page',
  templateUrl: './subscriptions.page.html',
  styleUrls: ['./subscriptions.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SubscriptionsPage implements OnInit, OnDestroy {
  busy = false;
  private subscriptions: Subscription[] = [];

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly router: Router,
    private readonly sourceSubscriptionService: SourceSubscriptionService,
  ) {}

  ngOnInit() {
    this.subscriptions.push();

    this.changeRef.detectChanges();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
