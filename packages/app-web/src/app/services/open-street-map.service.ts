import { Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { getSupportedPlaces, NamedLatLon } from '../products/upcoming/places';
import { convertOsmMatchToString } from '../products/upcoming/upcoming-header/upcoming-header.component';

export interface OsmMatch {
  lat: string;
  lon: string;
  display_name?: string;
  importance?: number;
  address: {
    'ISO3166-2-lvl4'?: string;
    country?: string;
    neighbourhood?: string;
    city_district?: string;
    country_code: string;
    state_district?: string;
    county?: string;
    postcode?: string;
    state: string;
    town?: string;
    village: string;
    amenity?: string;
    house_number?: string;
    road?: string;
  };
}

@Injectable({
  providedIn: 'root',
})
export class OpenStreetMapService {
  constructor(private readonly httpClient: HttpClient) {}

  // https://nominatim.openstreetmap.org/search?q=Innsbruck&format=json&addressdetails=1
  async searchByObject({
    state,
    place,
    country,
  }: Pick<NamedLatLon, 'state' | 'place' | 'country'>): Promise<NamedLatLon[]> {
    const matches = getSupportedPlaces().filter(
      (p) => p.state === state && p.place === place && p.country === country,
    );
    if (matches.length > 0) {
      return matches;
    } else {
      return this.searchByQuery(`${state} ${country} ${place}`);
    }
  }
  async searchByQuery(query: string): Promise<NamedLatLon[]> {
    const url = `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(
      query,
    )}&format=json&polygon=1&addressdetails=1`;
    return firstValueFrom(this.httpClient.get<OsmMatch[]>(url)).then(
      (matches) =>
        matches.map<NamedLatLon>((match) => ({
          lat: parseFloat(match.lat),
          lon: parseFloat(match.lon),
          state: match.address.country_code,
          country: match.address.state,
          place: match.address.county,
          displayName: convertOsmMatchToString(match),
        })),
    );
  }

  async reverseSearch(lat: number | string, lon: number | string) {
    const url = `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lon}&format=json`;
    return firstValueFrom(this.httpClient.get<OsmMatch>(url));
  }
}
