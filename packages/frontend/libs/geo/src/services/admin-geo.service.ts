import { inject, Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { NamedLatLon } from '@feedless/core';
import { getCachedLocations } from '../lib/places';
import { GeoSearchService } from './geo-search.interface';

const SEARCH_SERVER =
  'https://api3.geo.admin.ch/rest/services/ech/SearchServer';
const MAP_SERVER = 'https://api3.geo.admin.ch/rest/services/ech/MapServer';

interface SearchResultAttrs {
  detail?: string;
  label?: string;
  lat?: number;
  lon?: number;
  x?: number;
  y?: number;
  origin?: string;
}

interface SearchResult {
  id: number;
  weight: number;
  attrs: SearchResultAttrs;
}

interface SearchServerResponse {
  results: SearchResult[];
}

interface IdentifyResultAttributes {
  gemname?: string;
  kanton?: string;
  label?: string;
  plz?: string;
  ortbez?: string;
  [key: string]: unknown;
}

interface IdentifyResult {
  layerBodId: string;
  featureId: string;
  attributes: IdentifyResultAttributes;
}

interface IdentifyResponse {
  results: IdentifyResult[];
}

function stripHtml(html: string): string {
  return html
    .replace(/<[^>]*>/g, ' ')
    .replace(/\s+/g, ' ')
    .trim();
}

@Injectable({
  providedIn: 'root',
})
export class AdminGeoService implements GeoSearchService {
  private readonly httpClient = inject(HttpClient);

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
    }
    return this.searchByQuery(`${countryCode} ${area} ${place}`);
  }

  async searchByQuery(query: string): Promise<NamedLatLon[]> {
    const params = new URLSearchParams({
      searchText: query,
      type: 'locations',
      sr: '4326',
      limit: '5',
    });
    const url = `${SEARCH_SERVER}?${params.toString()}`;
    return firstValueFrom(this.httpClient.get<SearchServerResponse>(url)).then(
      (res) =>
        (res.results ?? []).map((r) => this.mapSearchResultToNamedLatLon(r)),
    );
  }

  async reverseSearch(
    lat: number | string,
    lon: number | string,
  ): Promise<NamedLatLon> {
    const latNum = typeof lat === 'string' ? parseFloat(lat) : lat;
    const lonNum = typeof lon === 'string' ? parseFloat(lon) : lon;
    const params = new URLSearchParams({
      geometryType: 'esriGeometryPoint',
      geometry: `${lonNum},${latNum}`,
      sr: '4326',
      tolerance: '0',
      imageDisplay: '0,0,0',
      mapExtent: '0,0,0,0',
      layers:
        'all:ch.swisstopo.swissboundaries3d-gemeinde-flaeche.fill,ch.swisstopo-vd.ortschaftenverzeichnis_plz',
      returnGeometry: 'false',
    });
    const url = `${MAP_SERVER}/identify?${params.toString()}`;
    return firstValueFrom(this.httpClient.get<IdentifyResponse>(url)).then(
      (res) => this.mapIdentifyResponseToNamedLatLon(res, latNum, lonNum),
    );
  }

  private mapSearchResultToNamedLatLon(result: SearchResult): NamedLatLon {
    const a = result.attrs;
    const lat = a.lat ?? a.y ?? 0;
    const lng = a.lon ?? a.x ?? 0;
    const detail = a.detail ?? '';
    return {
      lat: typeof lat === 'number' ? lat : parseFloat(String(lat)),
      lng: typeof lng === 'number' ? lng : parseFloat(String(lng)),
      countryCode: 'ch',
      place: detail.split(' ')[0] ?? detail,
      area: detail.split(' ')[1] ?? '',
      displayName: a.label,
    };
  }

  private mapIdentifyResponseToNamedLatLon(
    res: IdentifyResponse,
    lat: number,
    lon: number,
  ): NamedLatLon {
    const results = res.results ?? [];
    let place = '';
    let area = '';
    let zip: string | undefined;

    const gemeinde = results.find(
      (r) =>
        r.layerBodId === 'ch.swisstopo.swissboundaries3d-gemeinde-flaeche.fill',
    );
    const plzLayer = results.find(
      (r) => r.layerBodId === 'ch.swisstopo-vd.ortschaftenverzeichnis_plz',
    );

    if (gemeinde?.attributes) {
      place = gemeinde.attributes.gemname ?? gemeinde.attributes.label ?? '';
      area = gemeinde.attributes.kanton ?? '';
    }
    if (plzLayer?.attributes) {
      zip =
        plzLayer.attributes.plz ??
        (plzLayer.attributes.ortbez as string | undefined);
      if (!place && plzLayer.attributes.ortbez) {
        place = String(plzLayer.attributes.ortbez);
      }
    }

    const displayName = [zip, place, area].filter(Boolean).join(' ') || 'CH';
    return {
      lat,
      lng: lon,
      countryCode: 'ch',
      place: place || 'Switzerland',
      area: area || '',
      zip,
      displayName: `CH, ${displayName}`,
    };
  }
}
