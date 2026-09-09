import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { IonContent } from '@ionic/angular/standalone';
import dayjs from 'dayjs';
import { WebPage } from 'schema-dts';
// eslint-disable-next-line @nx/enforce-module-boundaries
import { PageService, PageTags } from '@feedless/components';
import { getCachedLocations } from '@feedless/geo';
import { UpcomingFooterComponent } from '../../components/upcoming-footer/upcoming-footer.component';

export type RegionSummary = {
  area: string;
  placeCount: number;
  url: string;
};

const BASE_URL = 'https://lokale.events';
const COUNTRY_PATH = '/events/in/CH';

/**
 * Oberste Hub-Ebene. Rendert ausschliesslich aus der statischen Ortsliste, also
 * ohne Resolver und ohne GraphQL - die Seite ist damit SSR-sicher und kann
 * nicht an einer Backend-Störung scheitern.
 */
@Component({
  selector: 'app-country-hub-page',
  templateUrl: './country-hub.page.html',
  styleUrls: ['./country-hub.page.scss'],
  imports: [IonContent, RouterLink, UpcomingFooterComponent],
  standalone: true,
})
export class CountryHubPage implements OnInit {
  private readonly pageService = inject(PageService);

  readonly regions: RegionSummary[] = this.collectRegions();

  ngOnInit(): void {
    this.pageService.setMetaTags(this.getPageTags());
    this.pageService.setJsonLdData(this.createSchema());
  }

  private collectRegions(): RegionSummary[] {
    const byArea = new Map<string, Set<string>>();
    for (const location of getCachedLocations()) {
      if (!byArea.has(location.area)) {
        byArea.set(location.area, new Set());
      }
      byArea.get(location.area).add(location.place);
    }
    return [...byArea.entries()]
      .map(([area, places]) => ({
        area,
        placeCount: places.size,
        url: `${COUNTRY_PATH}/${area}`,
      }))
      .sort((a, b) => a.area.localeCompare(b.area));
  }

  private getPageTags(): PageTags {
    const url = `${BASE_URL}${COUNTRY_PATH}`;
    return {
      title: 'Veranstaltungen in der Schweiz | lokale.events',
      description: `Lokale Veranstaltungen aus ${this.regions.length} Kantonen. Wähle deinen Kanton und finde Events in deiner Nähe.`,
      publisher: 'lokale.events',
      category: 'Events',
      url,
      canonicalUrl: url,
      lang: 'de',
      publishedAt: dayjs(),
      author: 'lokale.events Team',
      robots: 'index, follow',
    };
  }

  private createSchema(): WebPage {
    const url = `${BASE_URL}${COUNTRY_PATH}`;
    return {
      '@type': 'WebPage',
      name: 'Veranstaltungen in der Schweiz',
      url,
      inLanguage: 'de-CH',
      breadcrumb: {
        '@type': 'BreadcrumbList',
        itemListElement: [
          {
            '@type': 'ListItem',
            position: 1,
            item: { '@id': url, name: 'Veranstaltungen in der Schweiz' },
          },
        ],
      },
      mainEntity: {
        '@type': 'ItemList',
        name: 'Kantone',
        itemListElement: this.regions.map((region, index) => ({
          '@type': 'ListItem',
          position: index + 1,
          item: {
            '@id': `${BASE_URL}${region.url}`,
            name: `Veranstaltungen im Kanton ${region.area}`,
          },
        })),
      },
    };
  }
}
