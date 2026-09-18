import { Component, ChangeDetectionStrategy } from '@angular/core';
import { IonHeader, IonToolbar } from '@ionic/angular/standalone';

@Component({
  selector: 'app-promotion-header',
  templateUrl: './promotion-header.component.html',
  styleUrls: ['./promotion-header.component.scss'],
  imports: [IonHeader, IonToolbar],
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: true,
})
export class PromotionHeaderComponent {
  isRoot(): boolean {
    return location.pathname === '/';
  }
}
