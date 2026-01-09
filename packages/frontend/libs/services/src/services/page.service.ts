import { inject, Injectable, PLATFORM_ID } from '@angular/core';
import { Meta, MetaDefinition, Title } from '@angular/platform-browser';
import { Dayjs } from 'dayjs';
import { BreadcrumbList, Event, WebPage } from 'schema-dts';
import { LatLng } from '@feedless/core';
import { isPlatformBrowser } from '@angular/common';

export type PageTags = {
  title: string;
  lang: string;
  description: string;
  publisher: string;
  category?: string;
  url: string;
  region?: string;
  place?: string;
  position?: LatLng;
  publishedAt: Dayjs;
  startingAt?: Dayjs;
  expiresAt?: Dayjs;
  keywords?: string[];
  image?: string;
  author?: string;
  canonicalUrl?: string;
  robots?: string;
  viewport?: string;
};

@Injectable({
  providedIn: 'root',
})
export class PageService {
  private readonly meta = inject(Meta);
  private readonly title = inject(Title);
  private readonly platformId = inject(PLATFORM_ID);

  setMetaTags(options: PageTags) {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }
    // Clear existing meta tags to prevent duplicates
    this.clearMetaTags();

    this.title.setTitle(options.title);
    document
      .getElementsByTagName('html')
      .item(0)
      .setAttribute('lang', options.lang);

    // Basic meta tags
    const basicTags: MetaDefinition[] = [
      { name: 'title', content: options.title },
      { name: 'description', content: options.description },
      { name: 'publisher', content: options.publisher },
      { name: 'date', content: options.publishedAt.format('YYYY-MM-DD') },
      { name: 'robots', content: options.robots || 'index, follow' },
      {
        name: 'viewport',
        content: options.viewport || 'width=device-width, initial-scale=1.0',
      },
    ];

    // Add keywords if provided
    if (options.keywords && options.keywords.length > 0) {
      basicTags.push({
        name: 'keywords',
        content: options.keywords.join(', '),
      });
    }

    // Add author if provided
    if (options.author) {
      basicTags.push({ name: 'author', content: options.author });
    }

    if (options.expiresAt) {
      basicTags.push({
        name: 'expires',
        content: options.expiresAt.format('YYYY-MM-DD'),
      });
    } else {
      if (options.startingAt) {
        basicTags.push({
          name: 'expires',
          content: options.startingAt.add(1, 'month').format('YYYY-MM-DD'),
        });
      }
    }

    this.addMetaTags(basicTags);
    this.setCanonicalUrl(options.canonicalUrl || options.url);
    this.setOpenGraphTags(options);
    this.setTwitterCardTags(options);
    this.setGeoTags(options);
  }

  private setOpenGraphTags(options: PageTags) {
    const ogTags: MetaDefinition[] = [
      { property: 'og:site_name', content: 'lokale.events' },
      { property: 'og:title', content: options.title },
      { property: 'og:description', content: options.description },
      { property: 'og:url', content: options.url },
      { property: 'og:type', content: 'website' },
      { property: 'og:locale', content: 'de_DE' },
    ];

    // Add image if provided
    if (options.image) {
      ogTags.push({ property: 'og:image', content: options.image });
      ogTags.push({ property: 'og:image:alt', content: options.title });
    }

    // Add category if provided
    if (options.category) {
      ogTags.push({ property: 'og:category', content: options.category });
    }

    this.addMetaTags(ogTags);
  }

  private setTwitterCardTags(options: PageTags) {
    const twitterTags: MetaDefinition[] = [
      { name: 'twitter:card', content: 'summary_large_image' },
      { name: 'twitter:title', content: options.title },
      { name: 'twitter:description', content: options.description },
      { name: 'twitter:site', content: '@damoeb' },
      { name: 'twitter:creator', content: '@damoeb' },
    ];

    // Add image if provided
    if (options.image) {
      twitterTags.push({ name: 'twitter:image', content: options.image });
      twitterTags.push({ name: 'twitter:image:alt', content: options.title });
    }

    this.addMetaTags(twitterTags);
  }

  private setGeoTags(options: PageTags) {
    const geoTags: MetaDefinition[] = [];
    if (options.region) {
      geoTags.push({ name: 'geo.region', content: options.region });
    }
    if (options.place) {
      geoTags.push({ name: 'geo.placename', content: options.place });
    }
    if (options.position) {
      geoTags.push({
        name: 'geo.position',
        content: `${options.position.lat};${options.position.lng}`,
      });
      geoTags.push({
        name: 'ICBM',
        content: `${options.position.lat}, ${options.position.lng}`,
      });
    }

    if (geoTags.length > 0) {
      this.addMetaTags(geoTags);
    }
  }

  private addMetaTags(definitions: MetaDefinition[]) {
    this.meta.addTags(definitions);
  }

  private clearMetaTags() {
    // Remove existing meta tags to prevent duplicates
    const existingTags = document.querySelectorAll(
      'meta[name], meta[property]',
    );
    existingTags.forEach((tag) => {
      const name = tag.getAttribute('name') || tag.getAttribute('property');
      if (
        name &&
        (name.startsWith('og:') ||
          name.startsWith('twitter:') ||
          [
            'description',
            'keywords',
            'author',
            'robots',
            'geo.region',
            'geo.placename',
            'geo.position',
            'ICBM',
          ].includes(name))
      ) {
        tag.remove();
      }
    });

    // Remove existing JSON-LD scripts
    const existingJsonLd = document.querySelectorAll(
      'script[type="application/ld+json"]',
    );
    existingJsonLd.forEach((script) => script.remove());

    // Remove existing canonical links
    const existingCanonical = document.querySelectorAll(
      'link[rel="canonical"]',
    );
    existingCanonical.forEach((link) => link.remove());
  }

  private setCanonicalUrl(url: string) {
    const link = document.createElement('link');
    link.setAttribute('rel', 'canonical');
    link.setAttribute('href', url);
    document.head.append(link);
  }

  setJsonLdData(data: WebPage | BreadcrumbList | Event) {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }
    // Add structured data with caching to prevent duplicates
    const dataString = JSON.stringify(data, null, 2);
    const existingScript = document.querySelector(
      `script[type="application/ld+json"][data-content="${btoa(dataString)}"]`,
    );

    if (!existingScript) {
      const script = document.createElement('script');
      script.setAttribute('type', 'application/ld+json');
      script.setAttribute('data-content', btoa(dataString)); // Base64 encode for comparison
      script.textContent = dataString;
      document.head.append(script);
    }
  }
}
