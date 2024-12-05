import { circle, Circle, Map, Marker, marker, tileLayer } from 'leaflet';

import {
  AfterViewInit,
  Component,
  ElementRef,
  input,
  OnChanges,
  output,
  SimpleChanges,
  viewChild,
  ViewEncapsulation,
} from '@angular/core';
import { LatLon } from '../../types';

@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.scss'],
  encapsulation: ViewEncapsulation.None,
  standalone: true,
})
export class MapComponent implements AfterViewInit, OnChanges {
  readonly mapElement = viewChild<ElementRef>('map');

  readonly position = input.required<LatLon>();

  readonly perimeter = input.required<number>();

  readonly positionChange = output<LatLon>();

  private map: Map;
  private marker: Marker<any>;
  private circle: Circle<any>;

  constructor() {}

  ngAfterViewInit() {
    try {
      const maxZoom = 13;
      const minZoom = 11;
      const lat = this.position().lat;
      const lng = this.position().lon;
      this.map = new Map(this.mapElement().nativeElement)
        .setMinZoom(minZoom)
        .setMaxZoom(maxZoom)
        .setZoom(11)
        .setView([lat, lng], minZoom)
        .panTo({ lat, lng: lng - 0.05 });

      // https://stackoverflow.com/questions/18388288/how-do-you-add-marker-to-map-using-leaflet-map-onclick-function-event-handl

      tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        noWrap: true,
        keepBuffer: 4,
        minZoom,
        maxZoom,
      }).addTo(this.map);

      this.marker = marker({ lat, lng }).addTo(this.map);
      this.circle = circle(
        { lat, lng },
        { radius: this.perimeter() * 1000 },
      ).addTo(this.map);
      window.dispatchEvent(new Event('resize'));
    } catch (e) {
      console.error(e);
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.perimeter?.currentValue) {
      // this.circle.setRadius(changes.perimeter?.currentValue * 1000);
    }
  }
}
