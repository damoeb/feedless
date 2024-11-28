import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { FieldWrapper, Scalars } from '../../../generated/graphql';
import { Record } from '../../graphql/types';
import { dateFormat } from '../../services/session.service';
import { IonItem, IonLabel, IonChip } from '@ionic/angular/standalone';
import { NgIf, NgFor, DatePipe } from '@angular/common';
import { PlayerComponent } from '../player/player.component';

@Component({
  selector: 'app-remote-feed-item',
  templateUrl: './remote-feed-item.component.html',
  styleUrls: ['./remote-feed-item.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonItem, IonLabel, NgIf, NgFor, IonChip, PlayerComponent, DatePipe],
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
