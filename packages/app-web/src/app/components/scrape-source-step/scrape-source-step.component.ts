import { Component, EventEmitter, Output } from '@angular/core';

@Component({
  selector: 'app-scrape-source-step',
  templateUrl: './scrape-source-step.component.html',
  styleUrls: ['./scrape-source-step.component.scss']
})
export class ScrapeSourceStepComponent {
  @Output()
  delete: EventEmitter<void> = new EventEmitter<void>();
}
