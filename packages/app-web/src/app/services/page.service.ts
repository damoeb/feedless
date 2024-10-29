import { Injectable } from '@angular/core';
import {
  FeatureGroups,
  GqlFeatureGroupsQuery,
  GqlFeatureGroupsQueryVariables,
  GqlFeatureGroupWhereInput,
  GqlUpdateFeatureValueInput,
  GqlUpdateFeatureValueMutation,
  GqlUpdateFeatureValueMutationVariables,
  UpdateFeatureValue,
} from '../../generated/graphql';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import { FeatureGroup } from '../graphql/types';
import { Meta, Title } from '@angular/platform-browser';
import { LatLon } from '../components/map/map.component';

export type PageOptions = {
  title: string;
  description: string;
  publisher: string;
  category: string;
  url: string;
  region: string;
  place: string;
  position: LatLon;
}

@Injectable({
  providedIn: 'root',
})
export class PageService {
  constructor(private readonly meta: Meta,
              private readonly title: Title) {}

  setMetaTags(options: PageOptions) {
    this.title.setTitle(options.title)
    this.meta.addTags([
      { name: 'title', content: options.title },
      { name: 'description', content: options.description },
      { name: 'publisher', content: options.publisher },
    ]);
    this.setOpenGraphTags(options);
    this.setTwitterCardTags(options);
    this.setGeoTags(options);

    /*
    <link rel="canonical" href="https://www.example.com/page-url" />
    <link rel="alternate" hreflang="en" href="https://www.example.com/en/">
    <link rel="alternate" hreflang="es" href="https://www.example.com/es/">
    <link rel="alternate" hreflang="fr" href="https://www.example.com/fr/">
    <link rel="alternate" hreflang="x-default" href="https://www.example.com/">
     */
  }

  private setOpenGraphTags(options: PageOptions) {
    this.meta.addTags([
      { property: 'og:site_name', content: options.title },
      { property: 'og:title', content: options.title },
      { property: 'og:description', content: options.description },
      { property: 'og:category', content: options.category },
      { property: 'og:url', content: options.url },
      { property: 'og:type', content: 'website' }
    ]);
  }

  private setTwitterCardTags(options: PageOptions) {
    this.meta.addTags([
      { name: 'twitter:title', content: options.title },
      { name: 'twitter:description', content: options.description },
      { name: 'twitter:site', content: '@damoeb' },
    ]);
  }

  private setGeoTags(options: PageOptions) {
    this.meta.addTags([
      { name: 'geo.region', content: options.region },
      { name: 'geo.placename', content: options.place },
      { name: 'geo.position', content: `${options.position[0]};${options.position[1]}` }, // '40.7128;-74.0060'
      { name: 'ICBM', content: `${options.position[0]}, ${options.position[1]}`}, // '40.7128, -74.0060'
    ]);
  }
}
