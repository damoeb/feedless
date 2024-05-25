import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-repository-delivery-page',
  templateUrl: './repository-delivery.page.html',
  styleUrls: ['./repository-delivery.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RepositoryDeliveryPage implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];

  constructor(private readonly activatedRoute: ActivatedRoute) {}

  async ngOnInit() {
    this.subscriptions.push(
      this.activatedRoute.params.subscribe((params) => {}),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
