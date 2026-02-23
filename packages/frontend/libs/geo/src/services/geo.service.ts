import { inject, Injectable, PLATFORM_ID } from '@angular/core';
import { BehaviorSubject, map, Observable, Subject } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { NamedLatLon, Nullable } from '@feedless/core';
import { OpenStreetMapService } from './open-street-map.service';
import { getCachedLocations } from '../lib/places';
import { isPlatformBrowser } from '@angular/common';

export interface GeoJsResponse {
  longitude: string;
  country: string;
  timezone: string;
  city: string;
  organization: string;
  asn: number;
  accuracy: number;
  area_code: string;
  organization_name: string;
  country_code: string;
  ip: string;
  continent_code: string;
  country_code3: string;
  region: string;
  latitude: string;
}

@Injectable({
  providedIn: 'root',
})
export class GeoService {
  private readonly httpClient = inject(HttpClient);
  private readonly openStreetMapService = inject(OpenStreetMapService);
  private readonly platformId = inject(PLATFORM_ID);

  private currentPosition: Subject<NamedLatLon>;

  constructor() {
    const supported = getCachedLocations();
    this.currentPosition = new BehaviorSubject<Nullable<NamedLatLon>>(
      supported[Math.floor(Math.random() * supported.length + 1)],
    );
    if (isPlatformBrowser(this.platformId)) {
      this.getLocationFromIp().subscribe((value) =>
        this.currentPosition.next(value),
      );
    }
  }

  private getLocationFromIp(): Observable<NamedLatLon> {
    // https://www.geojs.io/docs/v1/endpoints/geo/
    return this.httpClient
      .get<GeoJsResponse>('https://get.geojs.io/v1/ip/geo.json')
      .pipe(
        map<GeoJsResponse, NamedLatLon>((response) => ({
          lat: parseFloat(response.latitude),
          lng: parseFloat(response.longitude),
          countryCode: response.country_code,
          place: response.city,
          area: response.region,
          displayName: `${response.city}, ${response.region}`,
        })),
      );
  }

  getCurrentLatLon(): Observable<NamedLatLon> {
    return this.currentPosition.asObservable();
  }

  requestLocationFromBrowser() {
    new Promise<GeolocationPosition>((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(resolve, reject);
    })
      .then((response) =>
        this.openStreetMapService.reverseSearch(
          response.coords.latitude,
          response.coords.longitude,
        ),
      )
      .then(this.currentPosition.next);
  }
}
