import { ChangeDetectionStrategy, Component } from '@angular/core';
import { IonHeader, IonContent } from '@ionic/angular/standalone';

@Component({
  selector: 'app-upcoming-terms-page',
  templateUrl: './terms.page.html',
  styleUrls: ['./terms.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonHeader, IonContent],
  standalone: true,
})
export class TermsPage {
  constructor() {}
}
