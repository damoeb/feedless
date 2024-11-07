import { Component, Input } from '@angular/core';
import { NamedLatLon } from '../places';

@Component({
  selector: 'app-upcoming-footer',
  templateUrl: './upcoming-footer.component.html',
  styleUrls: ['./upcoming-footer.component.scss'],
})
export class UpcomingFooterComponent {
  @Input({ required: true })
  location: NamedLatLon;

  constructor() {}
}
