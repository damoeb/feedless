import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-product-header',
  templateUrl: './product-header.component.html',
  styleUrls: ['./product-header.component.scss'],
})
export class ProductHeaderComponent {
  @Input({ required: true })
  title: string;

  constructor() {}
}
