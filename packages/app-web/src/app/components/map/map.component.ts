import { Map, marker, tileLayer } from 'leaflet';

import {
  AfterViewInit,
  Component,
  ElementRef,
  ViewChild,
  ViewEncapsulation,
} from '@angular/core';

@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.scss'],
  encapsulation: ViewEncapsulation.None,
})
export class MapComponent implements AfterViewInit {
  @ViewChild('map')
  mapElement: ElementRef;

  private map: Map;

  constructor() {}

  ngAfterViewInit() {
    try {
      const maxZoom = 13;
      const minZoom = 12;
      this.map = new Map(this.mapElement.nativeElement)
        .setMinZoom(minZoom)
        .setMaxZoom(maxZoom)
        .setView([47.371273, 8.53828], minZoom)
        .panTo({ lat: 47.371273, lng: 8.53828 - 0.05 });

      // https://stackoverflow.com/questions/18388288/how-do-you-add-marker-to-map-using-leaflet-map-onclick-function-event-handl

      tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        noWrap: true,
        minZoom,
        maxZoom,
      }).addTo(this.map);

      marker({ lat: 47.371273, lng: 8.53828 }).addTo(this.map);
    } catch (e) {
      console.error(e);
    }
  }
}
