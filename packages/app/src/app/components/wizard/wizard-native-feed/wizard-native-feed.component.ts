import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
} from '@angular/core';
import { WizardContext } from '../wizard/wizard.component';

@Component({
  selector: 'app-wizard-feed-items',
  templateUrl: './wizard-native-feed.component.html',
  styleUrls: ['./wizard-native-feed.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WizardNativeFeedComponent implements OnInit {
  @Input()
  context: WizardContext;

  @Output()
  updateContext: EventEmitter<Partial<WizardContext>> = new EventEmitter<
    Partial<WizardContext>
  >();

  feedUrl: string;

  constructor() {}

  ngOnInit() {
    this.feedUrl = this.context.feedUrl;
  }
}
