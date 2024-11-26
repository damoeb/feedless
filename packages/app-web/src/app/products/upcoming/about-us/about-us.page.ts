import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-upcoming-about-us-page',
  templateUrl: './about-us.page.html',
  styleUrls: ['./about-us.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class AboutUsPage {
  constructor() {}
}
