import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
} from '@angular/core';
import {
  GqlCompositeFilterParamsInput,
  GqlConditionalTagInput,
  GqlScrapeRequestInput,
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
  @Input()
  feed: RemoteFeed;
  protected busy: boolean = false;
  @Input()
  noMetaColumn: boolean = false;

  constructor(
    private readonly feedService: FeedService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async loadFeedPreview(
    requests: GqlScrapeRequestInput[],
    filters: GqlCompositeFilterParamsInput[],
    tags: GqlConditionalTagInput[],
  ) {
    this.busy = true;
    this.changeRef.detectChanges();
    try {
      this.feed = await this.feedService.previewFeed({
        requests,
        filters,
        tags,
      });
    } catch (e) {}
    this.busy = false;
    this.changeRef.detectChanges();
  }
}
