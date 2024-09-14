import { Component, Input } from '@angular/core';
import { RemoteFeed } from '../../graphql/types';

@Component({
  selector: 'app-remote-feed-preview',
  templateUrl: './remote-feed-preview.component.html',
  styleUrls: ['./remote-feed-preview.component.scss'],
})
export class RemoteFeedPreviewComponent {
  @Input()
  feed: RemoteFeed;
  protected busy: boolean = false;
  @Input()
  noMetaColumn: boolean = false;

  constructor(
  ) {}

}
