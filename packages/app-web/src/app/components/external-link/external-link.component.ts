import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-external-link',
  templateUrl: './external-link.component.html',
  styleUrls: ['./external-link.component.scss'],
})
export class ExternalLinkComponent {
  @Input()
  title: string;

  @Input()
  url: string;

  @Input()
  fill = 'outline';

  constructor() {}
}
