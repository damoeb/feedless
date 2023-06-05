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
