import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnInit,
} from '@angular/core';
import {
  Article,
  ArticleService,
  Content,
  Enclosure,
} from '../../services/article.service';
import { BasicNativeFeed, FeedService } from '../../services/feed.service';
import { ActivatedRoute } from '@angular/router';
import { SettingsService } from '../../services/settings.service';
import { GqlReleaseStatus } from '../../../generated/graphql';

@Component({
  selector: 'app-article',
  templateUrl: './article.component.html',
  styleUrls: ['./article.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ArticleComponent implements OnInit {
  @Input()
  article: Article;
  @Input()
  url: string;
  @Input()
  showDate: boolean;
  @Input()
  showThumbnail = true;
  @Input()
  targetBlank: boolean;

  audioStreams: Enclosure[] = [];
  videoStreams: Enclosure[] = [];

  feed: BasicNativeFeed;
  content: Content;
  bucketId: string;
  renderFulltext: boolean;

  constructor(
    private readonly articleService: ArticleService,
    private readonly changeRef: ChangeDetectorRef,
    private readonly activatedRoute: ActivatedRoute,
    private readonly settingsService: SettingsService,
    private readonly feedService: FeedService
  ) {}

  async ngOnInit() {
    this.renderFulltext = this.settingsService.useFulltext();

    if (this.article.nativeFeedId) {
      this.feed = await this.feedService.getNativeFeed({
        where: {
          id: this.article.nativeFeedId
        }
      }
      );
    }

    const content = this.article.content;
    this.content = content;

    if (content.enclosures) {
      this.audioStreams = content.enclosures.filter((enclosure) =>
        enclosure.type.startsWith('audio')
      );
      this.videoStreams = content.enclosures.filter((enclosure) =>
        enclosure.type.startsWith('video')
      );
    }
    this.changeRef.detectChanges();
  }

  statusToString(status: GqlReleaseStatus): string {
    switch (status) {
      case GqlReleaseStatus.NeedsApproval:
        return 'Pending';
      case GqlReleaseStatus.Released:
        return 'Published';
    }
  }

  createdAt(): Date {
    return new Date(this.content.publishedAt);
  }

  trimToFallback(actualValue: string, fallback: string): string {
    if (actualValue && actualValue.trim().length > 0) {
      return actualValue;
    }
    return fallback;
  }

  getColorForStatus() {
    if (this.article.status === GqlReleaseStatus.Released) {
      return 'success';
    } else {
      return 'warning';
    }
  }
}
