import { Component, input } from '@angular/core';
import { ProductHeadlineComponent } from '../product-headline/product-headline.component';

@Component({
  selector: 'app-product-header',
  templateUrl: './product-header.component.html',
  styleUrls: ['./product-header.component.scss'],
  imports: [ProductHeadlineComponent],
  standalone: true,
})
export class ProductHeaderComponent {
  readonly productTitle = input.required<string>();

  constructor() {}
}
