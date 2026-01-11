import {
  AfterViewInit,
  Component,
  ElementRef,
  inject,
  input,
  output,
  PLATFORM_ID,
  viewChild,
  ViewEncapsulation,
} from '@angular/core';
import { LatLng } from '@feedless/core';
import { isPlatformBrowser } from '@angular/common';
import type { LayerGroup, Map } from 'leaflet';

export type LatLngBoundingBox = { northEast: LatLng; southWest: LatLng };

@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  // styleUrls: ['./map.component.scss'],
  encapsulation: ViewEncapsulation.None,
  standalone: true,
})
export class MapComponent implements AfterViewInit {
  readonly mapElement = viewChild<ElementRef>('map');
  private readonly platformId = inject(PLATFORM_ID);

  readonly position = input.required<LatLng>();

  readonly perimeter = input<number>(10);
  readonly minZoom = input<number>(11);
  readonly maxZoom = input<number>(13);

  readonly boundingBoxChange = output<LatLngBoundingBox>();

  readonly positionChange = output<LatLng>();

  private map: Map;
  private markersLayer: LayerGroup<any>;
  // private marker: Marker<any>;
  // private circle: Circle<any>;

  async ngAfterViewInit() {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    try {
      const position = this.position();
      if (!position) {
        console.warn('Position not provided to map component');
        return;
      }

      const L = await import('leaflet');

      const lat = position.lat;
      const lng = position.lng;
      const minZoom = this.minZoom();
      const maxZoom = this.maxZoom();
      this.markersLayer = L.layerGroup();
      this.map = new L.Map(this.mapElement().nativeElement)
        .setMinZoom(minZoom)
        .setMaxZoom(maxZoom)
        .setZoom(11)
        .setView([lat, lng], minZoom)
        .addLayer(this.markersLayer)
        .panTo({ lat, lng: lng - 0.05 })
        .on('moveend', this.emitBoundingBox.bind(this))
        .on('zoomend', this.emitBoundingBox.bind(this));

      // https://stackoverflow.com/questions/18388288/how-do-you-add-marker-to-map-using-leaflet-map-onclick-function-event-handl

      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        noWrap: true,
        keepBuffer: 4,
        minZoom,
        maxZoom,
      }).addTo(this.map);

      // marker({ lat, lng }).addTo(this.map);
      // circle(
      //   { lat, lng },
      //   { radius: this.perimeter() * 1000 },
      // ).addTo(this.map);
      window.dispatchEvent(new Event('resize'));
    } catch (e) {
      console.error(e);
    }
  }

  getNativeMap(): Map {
    return this.map;
  }

  emitBoundingBox() {
    if (this.map) {
      const bounds = this.map.getBounds();
      const bbox: LatLngBoundingBox = {
        northEast: bounds.getNorthEast(),
        southWest: bounds.getSouthWest(),
      };
      this.boundingBoxChange.emit(bbox);
    }
  }
}
