import { Injectable } from '@angular/core';
import { Meta, MetaDefinition, Title } from '@angular/platform-browser';
import { LatLon } from '../components/map/map.component';
import { Dayjs } from 'dayjs';
import { WebPage, BreadcrumbList, Event } from 'schema-dts';

export type PageOptions = {
  title: string;
  lang: string;
  description: string;
  publisher: string;
  category?: string;
  url: string;
  region: string;
  place: string;
  position: LatLon;
  publishedAt: Dayjs;
  startingAt?: Dayjs;
};

@Injectable({
  providedIn: 'root',
})
export class PageService {
  constructor(
    private readonly meta: Meta,
    private readonly title: Title,
  ) {}

  setMetaTags(options: PageOptions) {
    this.title.setTitle(options.title);
    document
      .getElementsByTagName('html')
      .item(0)
      .setAttribute('lang', options.lang);
    this.addMetaTags([
      { name: 'title', content: options.title },
      { name: 'description', content: options.description },
      { name: 'publisher', content: options.publisher },
      { name: 'date', content: options.publishedAt.format('YYYY-MM-DD') }, // 2023-10-30
      {
        name: 'expires',
        content: options.startingAt?.add(1, 'month')?.format('YYYY-MM-DD'),
      }, // 2023-10-30
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
    this.addMetaTags([
      { property: 'og:site_name', content: options.title },
      { property: 'og:title', content: options.title },
      { property: 'og:description', content: options.description },
      // { property: 'og:category', content: options.category },
      { property: 'og:url', content: options.url },
      { property: 'og:type', content: 'website' },
    ]);
  }

  private setTwitterCardTags(options: PageOptions) {
    this.addMetaTags([
      { name: 'twitter:title', content: options.title },
      { name: 'twitter:description', content: options.description },
      { name: 'twitter:site', content: '@damoeb' },
    ]);
  }

  private setGeoTags(options: PageOptions) {
    this.addMetaTags([
      { name: 'geo.region', content: options.region },
      { name: 'geo.placename', content: options.place },
      {
        name: 'geo.position',
        content: `${options.position[0]};${options.position[1]}`,
      }, // '40.7128;-74.0060'
      {
        name: 'ICBM',
        content: `${options.position[0]}, ${options.position[1]}`,
      }, // '40.7128, -74.0060'
    ]);
  }

  private addMetaTags(definitions: MetaDefinition[]) {
    this.meta.addTags(definitions);
  }

  setJsonLdData(data: WebPage | BreadcrumbList | Event) {
    const script = document.createElement('script');
    script.setAttribute('type', 'application/ld+json');
    script.textContent = JSON.stringify(data, null, 2);
    document.head.append(script)
  }
}
