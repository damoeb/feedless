import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
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
} from '../upcoming-product-routing.module';
import { WebPage } from 'schema-dts';
import { createBreadcrumbsSchema } from '../events/events.page';
import { addIcons } from 'ionicons';
import { arrowBackOutline, calendarNumberOutline } from 'ionicons/icons';
import { NamedLatLon } from '../../../types';

import { UpcomingHeaderComponent } from '../upcoming-header/upcoming-header.component';
import {
  IonContent,
  IonSpinner,
  IonToolbar,
  IonButtons,
  IonButton,
  IonIcon,
  IonNote,
  IonBadge,
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
    UpcomingFooterComponent
],
  standalone: true,
})
export class EventPage implements OnInit, OnDestroy {
  loading: boolean = true;
  date: Dayjs;
  location: NamedLatLon;
  event: Record;

  private subscriptions: Subscription[] = [];

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly activatedRoute: ActivatedRoute,
    private readonly pageService: PageService,
    private readonly openStreetMapService: OpenStreetMapService,
    private readonly appConfigService: AppConfigService,
    private readonly recordService: RecordService,
  ) {
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
    return {
      '@type': 'WebPage',
      name: tags.title,
      description: tags.description,
      datePublished: tags.publishedAt.toISOString(),
      url: 'https://example.com/upcoming-events',
      breadcrumb: createBreadcrumbsSchema(this.location),
      mainEntity: {
        '@type': 'Event',
        location: 'Memphis, TN, US',
        startDate: '2011-05-20',
        url: 'foo-fighters-may20-fedexforum',
      },
    };
  }

  private getPageTags(): PageTags {
    return {
      title: `${this.event.title}`,
      description: `${this.event.text}`,
      publisher: 'upcoming',
      category: '',
      url: document.location.href,
      region: this.location.area,
      place: this.location.displayName,
      lang: 'de',
      publishedAt: dayjs(this.event.createdAt),
      position: [this.location.lat, this.location.lon],
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
GEO:${this.location.lat};${this.location.lon}
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
