import { Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { getCachedLocations } from '../products/upcoming/places';
import { compact, omit } from 'lodash-es';
import { NamedLatLon } from '../types';

interface OsmMatch {
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

export function convertOsmMatchToString(location: OsmMatch): string {
  if (!location) {
    return '';
  }
  const fields: (keyof OsmMatch['address'])[] = [
    'country',
    'country_code',
    'ISO3166-2-lvl4',
    'postcode',
    'state',
    'amenity',
    'house_number',
    'road',
    'county',
    'neighbourhood',
    'city_district',
    'state_district',
  ];
  return compact(Object.values(omit(location.address, ...fields))).join(' ');
}

export function convertOsmMatchToNamedLatLon(osmMatch: OsmMatch): NamedLatLon {
  const { country_code, state, postcode } = osmMatch.address;
  const place = convertOsmMatchToString(osmMatch);
  return {
    lat: parseFloat(osmMatch.lat),
    lon: parseFloat(osmMatch.lon),
    countryCode: country_code,
    area: state,
    place,
    displayName: `${country_code.toUpperCase()}, ${postcode} ${place}`,
  };
}

@Injectable({
  providedIn: 'root',
})
export class OpenStreetMapService {
  constructor(private readonly httpClient: HttpClient) {}

  // https://nominatim.openstreetmap.org/search?q=Innsbruck&format=json&addressdetails=1
  async searchByObject({
    countryCode,
    place,
    area,
  }: Pick<NamedLatLon, 'countryCode' | 'place' | 'area'>): Promise<
    NamedLatLon[]
  > {
    const matches = getCachedLocations().filter(
      (p) =>
        p.countryCode === countryCode && p.place === place && p.area === area,
    );
    if (matches.length > 0) {
      return matches;
    } else {
      debugger;
      return this.searchByQuery(`${countryCode} ${area} ${place}`);
    }
  }
  async searchByQuery(query: string): Promise<NamedLatLon[]> {
    const url = `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(
      query,
    )}&format=json&polygon=1&addressdetails=1&category=boundary&limit=5&addressdetails=1`;
    return firstValueFrom(this.httpClient.get<OsmMatch[]>(url)).then(
      (matches) => matches.map<NamedLatLon>(convertOsmMatchToNamedLatLon),
    );
  }

  async reverseSearch(
    lat: number | string,
    lon: number | string,
  ): Promise<NamedLatLon> {
    const url = `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lon}&format=json`;
    return firstValueFrom(this.httpClient.get<OsmMatch>(url)).then(
      convertOsmMatchToNamedLatLon,
    );
  }
}
