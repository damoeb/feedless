import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-upcoming-terms-page',
  templateUrl: './terms.page.html',
  styleUrls: ['./terms.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class TermsPage {
  constructor() {}
}
