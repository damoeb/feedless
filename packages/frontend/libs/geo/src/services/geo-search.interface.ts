import { NamedLatLon } from '@feedless/core';

export interface GeoSearchService {
  searchByObject(
    params: Pick<NamedLatLon, 'countryCode' | 'place' | 'area'>,
  ): Promise<NamedLatLon[]>;

  searchByQuery(query: string): Promise<NamedLatLon[]>;

  reverseSearch(
    lat: number | string,
    lon: number | string,
  ): Promise<NamedLatLon>;
}
