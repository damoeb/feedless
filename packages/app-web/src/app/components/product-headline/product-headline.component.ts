import { Component, input, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-product-headline',
  templateUrl: './product-headline.component.html',
  styleUrls: ['./product-headline.component.scss'],
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: true,
})
export class ProductHeadlineComponent {
  readonly title = input.required<string>();

  constructor() {}
}
