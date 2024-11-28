import { ChangeDetectionStrategy, Component } from '@angular/core';
import { IonContent, IonHeader } from '@ionic/angular/standalone';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-upcoming-about-us-page',
  templateUrl: './about-us.page.html',
  styleUrls: ['./about-us.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonHeader, IonContent, RouterLink],
  standalone: true,
})
export class AboutUsPage {
  constructor() {}
}
