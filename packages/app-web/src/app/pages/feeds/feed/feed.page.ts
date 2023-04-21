import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FeedService } from '../../../services/feed.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-feed-page',
  templateUrl: './feed.page.html',
  styleUrls: ['./feed.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeedPage implements OnInit, OnDestroy {
  id: string;

  private subscriptions: Subscription[] = [];

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly feedService: FeedService
  ) {}

  ngOnInit() {
    this.subscriptions.push(
      this.activatedRoute.params.subscribe((params) => {
        this.id = params.id;
      })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async delete() {
    await this.feedService.deleteNativeFeed(this.id);
  }
}
