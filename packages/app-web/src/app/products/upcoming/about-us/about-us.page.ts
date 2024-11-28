import { ChangeDetectionStrategy, Component } from '@angular/core';
import { IonHeader, IonContent } from '@ionic/angular/standalone';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-upcoming-about-us-page',
  templateUrl: './about-us.page.html',
  styleUrls: ['./about-us.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonHeader, IonContent, RouterLink],
})
export class AboutUsPage {
  constructor() {}
}
