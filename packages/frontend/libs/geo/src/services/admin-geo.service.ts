import { inject, Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { NamedLatLon } from '@feedless/core';
import { getCachedLocations } from '../lib/places';
import { GeoSearchService } from './geo-search.interface';

export const SEARCH_SERVER =
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

/**
 * Der SearchServer hebt den Treffer im `label` selbst hervor - `<b>Zug (ZG)</b>`,
 * teils mit einer Klassifizierung wie `<i>Ort</i>` davor. Ungefiltert landet
 * dieses Markup in `displayName` und damit im Seitentitel, in der
 * Meta-Description und im Fliesstext, wo Angular es escaped und der Nutzer
 * `<b>Zug</b>` als Text liest. Die Suchvorschläge im Header setzen ihre eigene
 * Hervorhebung, brauchen die der API also nicht.
 */
export function stripHtml(html: string | undefined): string {
  return (html ?? '')
    .replace(/<[^>]*>/g, ' ')
    .replace(/\s+/g, ' ')
    .trim();
}

/**
 * Ort und Kanton aus der SearchServer-Antwort.
 *
 * `detail` ist die normalisierte Suchform und durchgehend kleingeschrieben -
 * daraus gelesen erscheint auf der Seite „Veranstaltungen in bern". Das `label`
 * trägt die Schreibweise: der fett markierte Teil ist der Ortsname, das
 * `(XX)` der Kanton. `detail` bleibt der Rückfall, aber nur wenn sein zweites
 * Token wie ein Kantonskürzel aussieht - bei `<i>Bus</i> <b>Hedingen,
 * Hausacker</b>` wäre es sonst „HAUSACKER".
 */
export function parsePlaceAndArea(
  label: string | undefined,
  detail: string | undefined,
): { place: string; area: string } {
  const cleanLabel = stripHtml(label);
  const boldMatch = /<b>([\s\S]*?)<\/b>/.exec(label ?? '');
  const bold = stripHtml(boldMatch?.[1]);

  const boldWithCanton = /^(.*?)\s*\(([A-Za-z]{2})\)$/.exec(bold);
  if (boldWithCanton) {
    return {
      place: boldWithCanton[1],
      area: boldWithCanton[2].toUpperCase(),
    };
  }

  const detailTokens = stripHtml(detail).split(' ');
  const cantonFromLabel = /\(([A-Za-z]{2})\)/.exec(cleanLabel)?.[1];
  const cantonFromDetail = /^[A-Za-z]{2}$/.test(detailTokens[1] ?? '')
    ? detailTokens[1]
    : undefined;

  return {
    place: bold || detailTokens[0] || cleanLabel,
    area: (cantonFromLabel ?? cantonFromDetail ?? '').toUpperCase(),
  };
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
    // Place first: the SearchServer ranks by leading term, so
    // `CH Zürich Hedingen` returns the city of Zürich rather than Hedingen.
    return this.searchByQuery([place, area].filter(Boolean).join(' '));
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
    const { place, area } = parsePlaceAndArea(a.label, a.detail);
    return {
      lat: typeof lat === 'number' ? lat : parseFloat(String(lat)),
      lng: typeof lng === 'number' ? lng : parseFloat(String(lng)),
      countryCode: 'ch',
      place,
      area,
      displayName: stripHtml(a.label),
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
