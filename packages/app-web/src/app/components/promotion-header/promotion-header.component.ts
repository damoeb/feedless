import { Component } from '@angular/core';
import { IonHeader, IonToolbar } from '@ionic/angular/standalone';

@Component({
  selector: 'app-promotion-header',
  templateUrl: './promotion-header.component.html',
  styleUrls: ['./promotion-header.component.scss'],
  imports: [IonHeader, IonToolbar],
  standalone: true,
})
export class PromotionHeaderComponent {
  constructor() {}
}
