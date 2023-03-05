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
  selector: 'app-wizard-importer',
  templateUrl: './wizard-importer.component.html',
  styleUrls: ['./wizard-importer.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WizardImporterComponent implements OnInit {
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
