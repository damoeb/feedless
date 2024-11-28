import { Component } from '@angular/core';
import { IonContent, IonRouterOutlet } from '@ionic/angular/standalone';

@Component({
  selector: 'app-feedless-product-page',
  templateUrl: './feedless-product.page.html',
  styleUrls: ['./feedless-product.page.scss'],
  imports: [IonContent, IonRouterOutlet],
  standalone: true,
})
export class FeedlessProductPage {}
