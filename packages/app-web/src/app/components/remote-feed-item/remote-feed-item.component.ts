import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { FieldWrapper, Scalars } from '../../../generated/graphql';
import { Record } from '../../graphql/types';
import { dateFormat } from '../../services/session.service';
import {
  IonBadge,
  IonChip,
  IonItem,
  IonLabel,
} from '@ionic/angular/standalone';
import { DatePipe } from '@angular/common';
import { PlayerComponent } from '../player/player.component';

@Component({
  selector: 'app-remote-feed-item',
  templateUrl: './remote-feed-item.component.html',
  styleUrls: ['./remote-feed-item.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonItem, IonLabel, IonChip, PlayerComponent, DatePipe, IonBadge],
  standalone: true,
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
