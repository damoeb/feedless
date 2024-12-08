import { Component, Input, input } from '@angular/core';
import { Record } from '../../graphql/types';

import { IonItem, IonLabel, IonList } from '@ionic/angular/standalone';
import { RemoteFeedItemComponent } from '../remote-feed-item/remote-feed-item.component';

@Component({
  selector: 'app-remote-feed-preview',
  templateUrl: './remote-feed-preview.component.html',
  styleUrls: ['./remote-feed-preview.component.scss'],
  imports: [IonList, IonItem, IonLabel, RemoteFeedItemComponent],
  standalone: true,
})
export class RemoteFeedPreviewComponent {
  @Input()
  items: Record[];
  readonly noMetaColumn = input<boolean>(false);

  constructor() {}
}
