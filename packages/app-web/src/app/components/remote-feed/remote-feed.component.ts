import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit } from '@angular/core';
import { FieldWrapper, Scalars } from '../../../generated/graphql';
import { RemoteFeedItem } from '../../graphql/types';
import { FeedService } from '../../services/feed.service';

@Component({
  selector: 'app-remote-feed',
  templateUrl: './remote-feed.component.html',
  styleUrls: ['./remote-feed.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RemoteFeedComponent implements OnInit {
  @Input({ required: true })
  feedUrl: string;
  @Input({ required: true })
  title = 'Feed Preview';
  @Input()
  showTitle = true;

  loading: boolean;
  feedItems: Array<RemoteFeedItem>;
  errorMessage: string;

  constructor(
    private readonly feedService: FeedService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  ngOnInit() {
    return this.refresh();
  }

  toDate(date: FieldWrapper<Scalars['Long']['output']>): Date {
    return new Date(date);
  }

  async refresh() {
    await this.fetch(this.feedUrl);
  }

  private async fetch(nativeFeedUrl: string): Promise<void> {
    this.loading = true;
    this.feedItems = [];
    this.changeRef.detectChanges();
    try {
      this.feedItems = await this.feedService.remoteFeedContent({
        nativeFeedUrl,
      });
    } catch (e) {
      this.errorMessage = e.message;
    }
    this.loading = false;
    this.changeRef.detectChanges();
  }
}
