import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
} from '@angular/core';
import {
  GqlCompositeFilterParamsInput,
  GqlScrapeRequest,
} from '../../../generated/graphql';
import { FeedService } from '../../services/feed.service';
import { RemoteFeed } from '../../graphql/types';

@Component({
  selector: 'app-remote-feed-preview',
  templateUrl: './remote-feed-preview.component.html',
  styleUrls: ['./remote-feed-preview.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RemoteFeedPreviewComponent {
  protected previewFeed: RemoteFeed;
  protected busy: boolean = false;

  constructor(
    private readonly feedService: FeedService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async loadFeedPreview(
    requests: GqlScrapeRequest[],
    filters: GqlCompositeFilterParamsInput[],
  ) {
    this.busy = true;
    this.changeRef.detectChanges();
    try {
      this.previewFeed = await this.feedService.previewFeed({
        requests,
        filters,
      });
    } catch (e) {}
    this.busy = false;
    this.changeRef.detectChanges();
  }
}
