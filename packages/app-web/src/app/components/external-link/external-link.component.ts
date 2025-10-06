import { Component, input } from '@angular/core';
import { NgClass } from '@angular/common';

@Component({
  selector: 'app-external-link',
  templateUrl: './external-link.component.html',
  styleUrls: ['./external-link.component.scss'],
  standalone: true,
})
export class ExternalLinkComponent {
  readonly href = input.required<string>();
}
