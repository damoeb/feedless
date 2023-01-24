import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FeedService } from '../../services/feed.service';

@Component({
  selector: 'app-bucket',
  templateUrl: './feed.page.html',
  styleUrls: ['./feed.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeedPage implements OnInit {
  // feed: Bucket;
  // pagination: Pagination;
  id: string;
  name: string;
  constructor(private readonly activatedRoute: ActivatedRoute,
              private readonly feedService: FeedService) {}

  ngOnInit() {
    this.activatedRoute.params.subscribe((params) => {
      this.id = params.id;
    });
  }

  async delete() {
    await this.feedService.deleteNativeFeed(this.id);
  }
}
