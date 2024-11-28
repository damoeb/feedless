import { Component, Input } from '@angular/core';
import { Record } from '../../graphql/types';
import { NgIf, NgFor } from '@angular/common';
import { IonList, IonItem, IonLabel } from '@ionic/angular/standalone';
import { RemoteFeedItemComponent } from '../remote-feed-item/remote-feed-item.component';

@Component({
  selector: 'app-remote-feed-preview',
  templateUrl: './remote-feed-preview.component.html',
  styleUrls: ['./remote-feed-preview.component.scss'],
  imports: [NgIf, IonList, IonItem, IonLabel, NgFor, RemoteFeedItemComponent],
  standalone: true,
})
export class RemoteFeedPreviewComponent {
  @Input()
  items: Record[];
  @Input()
  noMetaColumn: boolean = false;

  constructor() {}
}
