import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnInit,
} from '@angular/core';
import { GqlArticleReleaseStatus } from '../../../generated/graphql';
import { BasicBucket } from '../../graphql/types';

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
