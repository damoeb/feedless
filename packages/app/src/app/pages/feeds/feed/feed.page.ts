import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FeedService } from '../../../services/feed.service';

@Component({
  selector: 'app-feed-page',
  templateUrl: './feed.page.html',
  styleUrls: ['./feed.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeedPage implements OnInit {
  id: string;
  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly feedService: FeedService
  ) {}

  ngOnInit() {
    this.activatedRoute.params.subscribe((params) => {
      this.id = params.id;
    });
  }

  async delete() {
    await this.feedService.deleteNativeFeed(this.id);
  }
}
