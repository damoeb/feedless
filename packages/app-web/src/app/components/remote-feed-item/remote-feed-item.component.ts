import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { FieldWrapper, Scalars } from '../../../generated/graphql';
import { Record } from '../../graphql/types';
import { dateFormat } from '../../services/session.service';

@Component({
  selector: 'app-remote-feed-item',
  templateUrl: './remote-feed-item.component.html',
  styleUrls: ['./remote-feed-item.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class RemoteFeedItemComponent {
  protected readonly dateFormat = dateFormat;

  @Input({ required: true })
  feedItem: Record;
  @Input({ required: true })
  feedItemIndex: number;

  constructor() {}

  toDate(date: FieldWrapper<Scalars['Long']['output']>): Date {
    return new Date(date);
  }
}
