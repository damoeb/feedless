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
  BasicContent,
} from '../../services/article.service';
import { BasicEnclosure, BasicNativeFeed } from '../../services/feed.service';
import { ActivatedRoute } from '@angular/router';
import { GqlArticleReleaseStatus } from '../../../generated/graphql';
import { ProfileService } from '../../services/profile.service';

export const getColorForArticleStatus = (status: GqlArticleReleaseStatus) => {
  if (status === GqlArticleReleaseStatus.Released) {
    return 'success';
  } else {
    return 'warning';
  }
};

export const articleStatusToString = (
  status: GqlArticleReleaseStatus
): string => {
  switch (status) {
    case GqlArticleReleaseStatus.Unreleased:
      return 'Pending';
    case GqlArticleReleaseStatus.Dropped:
      return 'Dropped';
    case GqlArticleReleaseStatus.Released:
      return 'Published';
  }
};

@Component({
  selector: 'app-article-ref',
  templateUrl: './article-ref.component.html',
  styleUrls: ['./article-ref.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ArticleRefComponent implements OnInit {
  @Input()
  article: Article;
  @Input()
  url: string;
  @Input()
  showDate: boolean;
  @Input()
  showStatus = true;
  @Input()
  showThumbnail = true;
  @Input()
  targetBlank: boolean;

  audioStreams: BasicEnclosure[] = [];
  videoStreams: BasicEnclosure[] = [];

  feed: BasicNativeFeed;
  content: BasicContent;
  bucketId: string;
  renderFulltext: boolean;

  constructor(
    private readonly articleService: ArticleService,
    private readonly changeRef: ChangeDetectorRef,
    private readonly activatedRoute: ActivatedRoute,
    private readonly profileService: ProfileService
  ) {}

  async ngOnInit() {
    this.renderFulltext = this.profileService.useFulltext();

    // if (this.article.nativeFeedId) {
    //   this.feed = await this.feedService.getNativeFeed({
    //     where: {
    //       id: this.article.nativeFeedId,
    //     },
    //   });
    // }

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

  statusToString(status: GqlArticleReleaseStatus): string {
    return articleStatusToString(status);
  }

  trimToFallback(actualValue: string, fallback: string): string {
    if (actualValue && actualValue.trim().length > 0) {
      return actualValue;
    }
    return fallback;
  }

  getUrl(): string {
    // return '/article/' + this.article.id;
    return this.article.content.url;
  }

  getColorForStatus() {
    return getColorForArticleStatus(this.article.status);
  }
}
