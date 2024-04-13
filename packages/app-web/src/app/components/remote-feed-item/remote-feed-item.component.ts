import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { FieldWrapper, Scalars } from '../../../generated/graphql';
import { RemoteFeedItem } from '../../graphql/types';
import { dateFormat } from '../../services/session.service';

@Component({
  selector: 'app-remote-feed-item',
  templateUrl: './remote-feed-item.component.html',
  styleUrls: ['./remote-feed-item.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RemoteFeedItemComponent {
  @Input({ required: true })
  feedItem: RemoteFeedItem;

  @Input({ required: true })
  feedItemIndex: number;
  constructor() {}

  toDate(date: FieldWrapper<Scalars['Long']['output']>): Date {
    return new Date(date);
  }

  protected readonly dateFormat = dateFormat;
}
