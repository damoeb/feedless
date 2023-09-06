import { Component, Input } from '@angular/core';
import { WizardHandler } from '../wizard-handler';

@Component({
  selector: 'app-wizard-feed-items',
  templateUrl: './wizard-native-feed.component.html',
  styleUrls: ['./wizard-native-feed.component.scss'],
})
export class WizardNativeFeedComponent {
  @Input()
  handler: WizardHandler;
  constructor() {}
}
