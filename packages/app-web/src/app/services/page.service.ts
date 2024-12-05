import { inject, Injectable } from '@angular/core';
import { Meta, MetaDefinition, Title } from '@angular/platform-browser';
import { Dayjs } from 'dayjs';
import { BreadcrumbList, Event, WebPage } from 'schema-dts';
import { LatLon } from '../types';

export type PageTags = {
  title: string;
  lang: string;
  description: string;
  publisher: string;
  category?: string;
  url: string;
  region?: string;
  place?: string;
  position?: LatLon;
  publishedAt: Dayjs;
  startingAt?: Dayjs;
};

@Injectable({
  providedIn: 'root',
})
export class PageService {
  private readonly meta = inject(Meta);
  private readonly title = inject(Title);

  setMetaTags(options: PageTags) {
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

  private setOpenGraphTags(options: PageTags) {
    this.addMetaTags([
      { property: 'og:site_name', content: options.title },
      { property: 'og:title', content: options.title },
      { property: 'og:description', content: options.description },
      // { property: 'og:category', content: options.category },
      { property: 'og:url', content: options.url },
      { property: 'og:type', content: 'website' },
    ]);
  }

  private setTwitterCardTags(options: PageTags) {
    this.addMetaTags([
      { name: 'twitter:title', content: options.title },
      { name: 'twitter:description', content: options.description },
      { name: 'twitter:site', content: '@damoeb' },
    ]);
  }

  private setGeoTags(options: PageTags) {
    const geoTags = [];
    if (options.region) {
      geoTags.push({ name: 'geo.region', content: options.region });
    }
    if (options.place) {
      geoTags.push({ name: 'geo.placename', content: options.place });
    }
    if (options.position) {
      geoTags.push({
        name: 'geo.position',
        content: `${options.position.lat};${options.position.lon}`, // '40.7128;-74.0060'
      });
      geoTags.push({
        name: 'ICBM',
        content: `${options.position.lat}, ${options.position.lon}`, // '40.7128, -74.0060'
      });
    }
  }

  private addMetaTags(definitions: MetaDefinition[]) {
    this.meta.addTags(definitions);
  }

  setJsonLdData(data: WebPage | BreadcrumbList | Event) {
    const script = document.createElement('script');
    script.setAttribute('type', 'application/ld+json');
    script.textContent = JSON.stringify(data, null, 2);
    document.head.append(script);
  }
}
