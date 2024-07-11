import { Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { HttpClient } from '@angular/common/http';

export interface OsmMatch {
  lat: string;
  lon: string;
  display_name?: string;
  importance?: number;
  address?: {
    'ISO3166-2-lvl4'?: string;
    country?: string;
    country_code?: string;
    county?: string;
    postcode: string;
    state?: string;
    town?: string;
    village?: string;
  };
}

@Injectable({
  providedIn: 'root',
})
export class OpenStreetMapService {
  constructor(private readonly httpClient: HttpClient) {}

  // https://nominatim.openstreetmap.org/search?q=Innsbruck&format=json&addressdetails=1
  async searchAddress(query: string) {
    const url = `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(
      query,
    )}&format=json&polygon=1&addressdetails=1`;
    return firstValueFrom(this.httpClient.get<OsmMatch[]>(url));
  }

  async reverseSearch(lat: number | string, lon: number | string) {
    const url = `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lon}&format=json`;
    return firstValueFrom(this.httpClient.get<OsmMatch>(url));
  }
}
