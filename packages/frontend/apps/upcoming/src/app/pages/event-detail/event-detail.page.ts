import { Component, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { IonContent } from '@ionic/angular/standalone';
import { isPlatformBrowser } from '@angular/common';
import dayjs, { Dayjs } from 'dayjs';
import 'dayjs/locale/de';
import { Event as SchemaEvent, WebPage } from 'schema-dts';
// eslint-disable-next-line @nx/enforce-module-boundaries
import { PageService, PageTags } from '@feedless/components';
import { NamedLatLon } from '@feedless/core';
import {
  EventDetailResolverData,
  renderEventUrl,
  renderPlaceUrl,
} from '../../upcoming-product-routes';
import { LocalizedEvent } from '../../event.service';
import { cleanEventTitle } from '../../event-title';
import { UpcomingFooterComponent } from '../../components/upcoming-footer/upcoming-footer.component';
import { isIndexable, ownText } from './event-content';

const BASE_URL = 'https://lokale.events';

@Component({
  selector: 'app-event-detail-page',
  templateUrl: './event-detail.page.html',
  styleUrls: ['./event-detail.page.scss'],
  imports: [IonContent, RouterLink, UpcomingFooterComponent],
  standalone: true,
})
export class EventDetailPage implements OnInit {
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly pageService = inject(PageService);
  private readonly platformId = inject(PLATFORM_ID);

  protected readonly isBrowser = isPlatformBrowser(this.platformId);

  event: LocalizedEvent | null = null;
  place: NamedLatLon | null = null;
  title = '';
  body = '';
  placeUrl = '';

  ngOnInit(): void {
    const data = this.activatedRoute.snapshot.data[
      'eventDetail'
    ] as EventDetailResolverData | null;

    if (data) {
      this.event = data.event;
      this.place = data.place;
      this.title = cleanEventTitle(data.event.title);
      this.body = ownText(data.event);
      this.placeUrl = renderPlaceUrl(
        data.place.countryCode,
        data.place.area,
        data.place.place,
      );
    }

    this.pageService.setMetaTags(this.getPageTags());
    if (this.event && this.place) {
      this.pageService.setJsonLdData(this.createSchema());
    }
  }

  get startDate(): Dayjs | null {
    return this.event?.startingAt ? dayjs(this.event.startingAt) : null;
  }

  /**
   * Nur das Datum, nie eine Uhrzeit. 96 % der Events stehen auf 10:00, weil die
   * Pipeline diesen Wert stempelt, wenn sie keine Zeit erkennt - eine
   * angezeigte Uhrzeit wäre für fast jedes Event erfunden.
   */
  formatDate(date: Dayjs | null): string {
    return date ? date.locale('de').format('dddd, D. MMMM YYYY') : '';
  }

  get isPast(): boolean {
    const date = this.startDate;
    return !!date && date.isBefore(dayjs().startOf('day'));
  }

  private canonicalUrl(): string {
    if (!this.event || !this.place) {
      return `${BASE_URL}/`;
    }
    return `${BASE_URL}${renderEventUrl(
      this.place.countryCode,
      this.place.area,
      this.place.place,
      this.event as { id: string; title?: string | null },
    )}`;
  }

  private getPageTags(): PageTags {
    if (!this.event || !this.place) {
      return {
        title: 'Veranstaltung nicht gefunden | lokale.events',
        description:
          'Diese Veranstaltung gibt es nicht mehr. Finde aktuelle Veranstaltungen in deiner Nähe.',
        publisher: 'lokale.events',
        url: `${BASE_URL}/`,
        lang: 'de-CH',
        locale: 'de_CH',
        publishedAt: dayjs(),
        robots: 'noindex, follow',
      };
    }

    const date = this.formatDate(this.startDate);
    return {
      title: `${this.title} in ${this.place.place} | lokale.events`,
      description:
        this.body ||
        `${this.title}${date ? ` am ${date}` : ''} in ${this.place.displayName}.`,
      publisher: 'lokale.events',
      category: 'Events',
      url: this.canonicalUrl(),
      canonicalUrl: this.canonicalUrl(),
      region: this.place.area,
      place: this.place.displayName,
      position: this.place,
      lang: 'de-CH',
      locale: 'de_CH',
      publishedAt: dayjs(),
      startingAt: this.startDate ?? undefined,
      author: 'lokale.events Team',
      robots: isIndexable(this.event)
        ? 'index, follow'
        : 'noindex, follow',
    };
  }

  private createSchema(): WebPage {
    const place = this.place;
    const event: SchemaEvent = {
      '@type': 'Event',
      name: this.title,
      description: this.body || `${this.title} in ${place.displayName}`,
      eventStatus: 'EventScheduled',
      eventAttendanceMode: 'OfflineEventAttendanceMode',
      // Reines Datum: die Uhrzeit im Bestand ist zu 96 % ein Default.
      startDate: this.startDate?.format('YYYY-MM-DD') ?? undefined,
      url: this.canonicalUrl(),
      location: {
        '@type': 'Place',
        name: place.displayName,
        address: {
          '@type': 'PostalAddress',
          addressLocality: place.place,
          addressRegion: place.area,
          addressCountry: place.countryCode,
        },
        geo: {
          '@type': 'GeoCoordinates',
          latitude: place.lat,
          longitude: place.lng,
        },
      },
    };

    return {
      '@type': 'WebPage',
      name: this.title,
      url: this.canonicalUrl(),
      inLanguage: 'de-CH',
      breadcrumb: {
        '@type': 'BreadcrumbList',
        itemListElement: [
          {
            '@type': 'ListItem',
            position: 1,
            item: {
              '@id': `${BASE_URL}${this.placeUrl}`,
              name: `Veranstaltungen in ${place.place}`,
            },
          },
          {
            '@type': 'ListItem',
            position: 2,
            item: { '@id': this.canonicalUrl(), name: this.title },
          },
        ],
      },
      mainEntity: event,
    };
  }
}
