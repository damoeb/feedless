import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import { WizardHandler } from '../wizard-handler';

@Component({
  selector: 'app-wizard-feed-items',
  templateUrl: './wizard-native-feed.component.html',
  styleUrls: ['./wizard-native-feed.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WizardNativeFeedComponent implements OnInit {
  @Input()
  handler: WizardHandler;

  feedUrl: string;

  constructor() {}

  ngOnInit() {
    this.feedUrl = this.handler.getContext().feedUrl;
  }
}
