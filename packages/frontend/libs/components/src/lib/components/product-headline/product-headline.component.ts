import { Component, input } from '@angular/core';

@Component({
  selector: 'app-product-headline',
  templateUrl: './product-headline.component.html',
  styleUrls: ['./product-headline.component.scss'],
  standalone: true,
})
export class ProductHeadlineComponent {
  readonly title = input.required<string>();
}
