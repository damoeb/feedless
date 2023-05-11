import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnInit,
} from '@angular/core';
import {
  GqlArticleReleaseStatus,
  GqlNativeFeedStatus,
} from '../../../generated/graphql';
import { BasicNativeFeed } from '../../graphql/types';

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
  selector: 'app-native-feed-ref',
  templateUrl: './native-feed-ref.component.html',
  styleUrls: ['./native-feed-ref.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NativeFeedRefComponent implements OnInit {
  @Input()
  feed: BasicNativeFeed;
  @Input()
  showTag = true;

  ngOnInit(): void {}

  hasProblems(status: GqlNativeFeedStatus): boolean {
    return [
      GqlNativeFeedStatus.NotFound,
      GqlNativeFeedStatus.NeverFetched,
      GqlNativeFeedStatus.Defective,
    ].includes(status);
  }
}
