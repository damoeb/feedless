import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { FieldWrapper, Record, Scalars } from '@feedless/graphql-api';
import { dateFormat } from '@feedless/services';
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

  readonly feedItem = input.required<Record>();
  readonly feedItemIndex = input.required<number>();

  constructor() {}

  toDate(date: FieldWrapper<Scalars['Long']['output']>): Date {
    return new Date(date);
  }
}
