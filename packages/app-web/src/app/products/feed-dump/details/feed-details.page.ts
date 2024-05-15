import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ServerSettingsService } from '../../../services/server-settings.service';
import { Subscription } from 'rxjs';
import { times } from 'lodash-es';

@Component({
  selector: 'app-feed-details-page',
  templateUrl: './feed-details.page.html',
  styleUrls: ['./feed-details.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeedDetailsPage implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];
  items: number[] = times(20);

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    readonly serverSettings: ServerSettingsService,
  ) {}

  ngOnInit() {
    this.subscriptions.push(
      this.activatedRoute.params.subscribe(async (params) => {
        if (params.id) {

        } else {

        }
      }),
    )
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

}
