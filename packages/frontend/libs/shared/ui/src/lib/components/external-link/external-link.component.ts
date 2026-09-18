import { Component, input, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-external-link',
  templateUrl: './external-link.component.html',
  styleUrls: ['./external-link.component.scss'],
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: true,
})
export class ExternalLinkComponent {
  readonly href = input.required<string>();
}
