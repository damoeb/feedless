import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { IonContent } from '@ionic/angular/standalone';
import dayjs from 'dayjs';
import { WebPage } from 'schema-dts';
// eslint-disable-next-line @nx/enforce-module-boundaries
import { PageService, PageTags } from '@feedless/components';
import { getCachedLocations } from '@feedless/geo';
import { renderPlaceUrl } from '../../upcoming-product-routes';
import { UpcomingFooterComponent } from '../../components/upcoming-footer/upcoming-footer.component';

export type PlaceSummary = {
  place: string;
  url: string;
};

const BASE_URL = 'https://lokale.events';
const COUNTRY_PATH = '/events/in/CH';

/**
 * Mittlere Hub-Ebene. Wie der Länder-Hub rein aus der statischen Ortsliste
 * gerendert, damit die Kette Land → Kanton → Ort auch bei Backend-Störungen
 * für Crawler begehbar bleibt.
 */
@Component({
  selector: 'app-region-hub-page',
  templateUrl: './region-hub.page.html',
  styleUrls: ['./region-hub.page.scss'],
  imports: [IonContent, RouterLink, UpcomingFooterComponent],
  standalone: true,
})
export class RegionHubPage implements OnInit {
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly pageService = inject(PageService);

  readonly region: string = this.activatedRoute.snapshot.params['region'] ?? '';
  readonly places: PlaceSummary[] = this.collectPlaces();

  ngOnInit(): void {
    this.pageService.setMetaTags(this.getPageTags());
    this.pageService.setJsonLdData(this.createSchema());
  }

  private collectPlaces(): PlaceSummary[] {
    const names = new Set(
      getCachedLocations()
        .filter(
          (location) =>
            location.area.toLowerCase() === this.region.toLowerCase(),
        )
        .map((location) => location.place),
    );
    return [...names]
      .sort((a, b) => a.localeCompare(b))
      .map((place) => ({
        place,
        url: renderPlaceUrl('CH', this.region, place),
      }));
  }

  private getPageTags(): PageTags {
    const url = `${BASE_URL}${COUNTRY_PATH}/${this.region}`;
    return {
      title: `Veranstaltungen im Kanton ${this.region} | lokale.events`,
      description: `Lokale Veranstaltungen in ${this.places.length} Orten im Kanton ${this.region}. Wähle deinen Ort und finde Events in deiner Nähe.`,
      publisher: 'lokale.events',
      category: 'Events',
      url,
      canonicalUrl: url,
      region: this.region,
      lang: 'de-CH',
      locale: 'de_CH',
      publishedAt: dayjs(),
      author: 'lokale.events Team',
      robots: 'index, follow',
    };
  }

  private createSchema(): WebPage {
    const countryUrl = `${BASE_URL}${COUNTRY_PATH}`;
    const url = `${countryUrl}/${this.region}`;
    return {
      '@type': 'WebPage',
      name: `Veranstaltungen im Kanton ${this.region}`,
      url,
      inLanguage: 'de-CH',
      breadcrumb: {
        '@type': 'BreadcrumbList',
        itemListElement: [
          {
            '@type': 'ListItem',
            position: 1,
            item: {
              '@id': countryUrl,
              name: 'Veranstaltungen in der Schweiz',
            },
          },
          {
            '@type': 'ListItem',
            position: 2,
            item: {
              '@id': url,
              name: `Veranstaltungen im Kanton ${this.region}`,
            },
          },
        ],
      },
      mainEntity: {
        '@type': 'ItemList',
        name: `Orte im Kanton ${this.region}`,
        itemListElement: this.places.map((place, index) => ({
          '@type': 'ListItem',
          position: index + 1,
          item: {
            '@id': `${BASE_URL}${place.url}`,
            name: `Veranstaltungen in ${place.place}`,
          },
        })),
      },
    };
  }
}
