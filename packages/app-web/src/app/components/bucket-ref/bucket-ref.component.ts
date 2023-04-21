import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnInit,
} from '@angular/core';
import { GqlArticleReleaseStatus } from '../../../generated/graphql';
import { BasicBucket } from '../../services/bucket.service';

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
  selector: 'app-bucket-ref',
  templateUrl: './bucket-ref.component.html',
  styleUrls: ['./bucket-ref.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BucketRefComponent implements OnInit {
  @Input()
  bucket: BasicBucket;

  ngOnInit(): void {}
}
