import { Component, Input } from '@angular/core';
import { Record } from '../../graphql/types';

@Component({
    selector: 'app-remote-feed-preview',
    templateUrl: './remote-feed-preview.component.html',
    styleUrls: ['./remote-feed-preview.component.scss'],
    standalone: false
})
export class RemoteFeedPreviewComponent {
  @Input()
  items: Record[];
  @Input()
  noMetaColumn: boolean = false;

  constructor() {}
}
