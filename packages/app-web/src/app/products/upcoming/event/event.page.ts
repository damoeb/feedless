import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { RecordService } from '../../../services/record.service';
import { Record } from '../../../graphql/types';
import { Subscription } from 'rxjs';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AppConfigService } from '../../../services/app-config.service';
import { PageService, PageTags } from '../../../services/page.service';
import dayjs, { Dayjs } from 'dayjs';
import { OpenStreetMapService } from '../../../services/open-street-map.service';
import {
  parseDateFromUrl,
  parseLocationFromUrl,
} from '../upcoming-product-routes';
import { WebPage } from 'schema-dts';
import { createBreadcrumbsSchema } from '../events/events.page';
import { addIcons } from 'ionicons';
import { arrowBackOutline, calendarNumberOutline } from 'ionicons/icons';
import { NamedLatLon } from '../../../types';

import { UpcomingHeaderComponent } from '../upcoming-header/upcoming-header.component';
import {
  IonBadge,
  IonButton,
  IonButtons,
  IonContent,
  IonIcon,
  IonNote,
  IonSpinner,
  IonToolbar,
} from '@ionic/angular/standalone';
import { UpcomingFooterComponent } from '../upcoming-footer/upcoming-footer.component';

@Component({
  selector: 'app-event-page',
  templateUrl: './event.page.html',
  styleUrls: ['./event.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    UpcomingHeaderComponent,
    IonContent,
    IonSpinner,
    IonToolbar,
    IonButtons,
    IonButton,
    RouterLink,
    IonIcon,
    IonNote,
    IonBadge,
    UpcomingFooterComponent,
  ],
  standalone: true,
})
export class EventPage implements OnInit, OnDestroy {
  private readonly changeRef = inject(ChangeDetectorRef);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly pageService = inject(PageService);
  private readonly openStreetMapService = inject(OpenStreetMapService);
  private readonly appConfigService = inject(AppConfigService);
  private readonly recordService = inject(RecordService);

  loading: boolean = true;
  date: Dayjs;
  location: NamedLatLon;
  event: Record;

  private subscriptions: Subscription[] = [];

  constructor() {
    addIcons({ arrowBackOutline, calendarNumberOutline });
  }

  async ngOnInit() {
    this.subscriptions.push(
      this.activatedRoute.params.subscribe(async (params) => {
        try {
          this.location = await parseLocationFromUrl(
            this.activatedRoute,
            this.openStreetMapService,
          );
          this.date = parseDateFromUrl(params);
          this.changeRef.detectChanges();

          const event = await this.recordService.findAllFullByRepositoryId(
            {
              where: {
                repository: {
                  id: this.appConfigService.customProperties
                    .eventRepositoryId as any,
                },
                id: {
                  eq: params.eventId,
                },
              },
              cursor: {
                page: 0,
              },
            },
            'cache-first',
          );
          this.event = event[0];
          this.loading = false;
          this.pageService.setMetaTags(this.getPageTags());
          this.pageService.setJsonLdData(this.createWebsiteSchema());
          this.changeRef.detectChanges();
        } catch (e) {
          console.error(e);
        }
      }),
    );
  }

  createWebsiteSchema(): WebPage {
    const tags = this.getPageTags();
    const startDate = dayjs(this.event.startingAt);

    return {
      '@type': 'WebPage',
      name: tags.title,
      description: tags.description,
      datePublished: tags.publishedAt.toISOString(),
      url: document.location.href,
      inLanguage: 'de-DE',
      breadcrumb: createBreadcrumbsSchema(this.location),
      mainEntity: {
        '@type': 'Event',
        name: this.event.title,
        description:
          this.event.text || `Veranstaltung in ${this.location?.displayName}`,
        startDate: startDate.toISOString(),
        eventStatus: 'EventScheduled',
        eventAttendanceMode: 'OfflineEventAttendanceMode',
        url: this.event.url,
        location: {
          '@type': 'Place',
          name: this.location?.displayName,
          address: {
            '@type': 'PostalAddress',
            addressLocality: this.location?.place,
            addressRegion: this.location?.area,
            addressCountry: this.location?.countryCode,
          },
          geo: {
            '@type': 'GeoCoordinates',
            latitude: this.location?.lat,
            longitude: this.location?.lng,
          },
        },
        organizer: {
          '@type': 'Organization',
          name: 'lokale.events',
          url: 'https://lokale.events',
        },
        offers: {
          '@type': 'Offer',
          availability: 'InStock',
          price: '0',
          priceCurrency: 'EUR',
          url: this.event.url,
        },
        keywords: this.event.tags?.join(', ') || '',
      },
      publisher: {
        '@type': 'Organization',
        name: 'lokale.events',
        url: 'https://lokale.events',
      },
    };
  }

  private getPageTags(): PageTags {
    const startDate = dayjs(this.event.startingAt);
    const keywords = [
      this.event.title,
      'Event',
      'Veranstaltung',
      this.location?.displayName,
      this.location?.area,
      this.location?.countryCode,
      startDate.format('DD.MM.YYYY'),
      'lokale Events',
      ...(this.event.tags || []),
    ].filter(Boolean);

    return {
      title: `${this.event.title} | Event in ${this.location?.displayName} | lokale.events`,
      description: `${this.event.text || 'Veranstaltung in ' + this.location?.displayName} am ${startDate.format('DD.MM.YYYY')}. Erfahre mehr über dieses Event in ${this.location?.displayName}, ${this.location?.area}.`,
      publisher: 'lokale.events',
      category: 'Event',
      url: document.location.href,
      region: this.location?.area,
      place: this.location?.displayName,
      lang: 'de',
      publishedAt: dayjs(this.event.createdAt),
      startingAt: startDate,
      position: this.location,
      keywords,
      author: 'lokale.events Team',
      robots: 'index, follow',
      canonicalUrl: document.location.href,
    };
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  downloadIcs() {
    const tags = this.getPageTags();
    const utcFormat = 'YYYYMMDD[T]HHmmss[Z]';
    const startingAt = dayjs(this.event.startingAt);
    const calendarEntry = `BEGIN:VCALENDAR
VERSION:2.0
PRODID:EVENTFROG//EVENT//ICAL
BEGIN:VEVENT
UID:${this.event.id}@lokale.events
DTSTAMP:${startingAt.format(utcFormat)}
DTSTART:${startingAt.format(utcFormat)}
DTEND:${startingAt.add(1, 'hour').format(utcFormat)}Z
SUMMARY:${tags.title}
DESCRIPTION:${tags.description}
GEO:${this.location.lat};${this.location.lng}
URL:${this.event.url}
END:VEVENT
END:VCALENDAR`;

    const element = document.createElement('a');
    element.setAttribute(
      'href',
      'data:text/calendar;charset=UTF-8,' + encodeURIComponent(calendarEntry),
    );
    element.setAttribute('download', 'foo.ics');

    element.style.display = 'none';
    document.body.appendChild(element);

    element.click();

    document.body.removeChild(element);
  }

  formatDate(date: number, format: string) {
    return dayjs(date).locale('de').format(format);
  }
}
