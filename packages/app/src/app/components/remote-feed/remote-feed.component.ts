import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
} from '@angular/core';
import { FeedService, RemoteFeedItem } from '../../services/feed.service';
import { FieldWrapper, Scalars } from '../../../generated/graphql';

@Component({
  selector: 'app-remote-feed',
  templateUrl: './remote-feed.component.html',
  styleUrls: ['./remote-feed.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RemoteFeedComponent implements OnInit, OnChanges {
  @Input()
  feedUrl: string;
  @Input()
  title = 'Feed Preview';
  @Input()
  showTitle = true;
  loading: boolean;
  feedItems: Array<RemoteFeedItem>;
  errorMessage: string;

  constructor(
    private readonly feedService: FeedService,
    private readonly changeRef: ChangeDetectorRef
  ) {}

  async ngOnInit(): Promise<void> {
    await this.fetch(this.feedUrl);
  }

  toDate(publishedAt: FieldWrapper<Scalars['Long']>): Date {
    return new Date(publishedAt);
  }

  async ngOnChanges(changes: SimpleChanges): Promise<void> {
    if (changes.feedUrl.currentValue) {
      await this.fetch(changes.feedUrl.currentValue);
    }
  }

  private async fetch(url: string): Promise<void> {
    this.loading = true;
    this.changeRef.detectChanges();
    try {
      this.feedItems = await this.feedService.remoteFeedContent(url);
    } catch (e) {
      this.errorMessage = e.message;
    }
    this.loading = false;
    this.changeRef.detectChanges();
  }
}
