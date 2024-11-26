import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-product-header',
  templateUrl: './product-header.component.html',
  styleUrls: ['./product-header.component.scss'],
  standalone: false,
})
export class ProductHeaderComponent {
  @Input({ required: true })
  productTitle: string;

  constructor() {}
}
