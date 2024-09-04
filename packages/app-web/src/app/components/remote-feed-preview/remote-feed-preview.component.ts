import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
} from '@angular/core';
import {
  GqlConditionalTagInput,
  GqlItemFilterParamsInput,
  GqlSourceInput,
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
    sources: GqlSourceInput[],
    filters: GqlItemFilterParamsInput[],
    // tags: GqlConditionalTagInput[],
  ) {
    this.busy = true;
    this.changeRef.detectChanges();
    try {
      const preview = await this.feedService.previewFeed({
        sources,
        filters,
        tags: [],
      });
      this.feed = preview.feed;
    } catch (e) {}
    this.busy = false;
    this.changeRef.detectChanges();
  }
}
