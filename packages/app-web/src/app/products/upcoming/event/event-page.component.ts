import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { RecordService } from '../../../services/record.service';
import { Record } from '../../../graphql/types';
import { Subscription } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { AppConfigService } from '../../../services/app-config.service';
import { PageService } from '../../../services/page.service';
import { Dayjs } from 'dayjs';
import { LatLon } from '../../../components/map/map.component';
import { OpenStreetMapService, OsmMatch } from '../../../services/open-street-map.service';
import { parseDateFromUrl, parseLocationFromUrl, parsePerimeterFromUrl } from '../upcoming-product-routing.module';

@Component({
  selector: 'app-event-page',
  templateUrl: './event-page.component.html',
  styleUrls: ['./event-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EventPageComponent implements OnInit, OnDestroy {

  loading: boolean = true;
  date: Dayjs;
  location: OsmMatch;
  event: Record;

  private subscriptions: Subscription[] = [];

  constructor(private readonly changeRef: ChangeDetectorRef,
              private readonly activatedRoute: ActivatedRoute,
              private readonly pageService: PageService,
              private readonly openStreetMapService: OpenStreetMapService,
              private readonly appConfigService: AppConfigService,
              private readonly recordService: RecordService) {}

  async ngOnInit() {
    this.subscriptions.push(
      this.activatedRoute.params.subscribe(async params => {
        this.location = await parseLocationFromUrl(this.activatedRoute, this.openStreetMapService);
        this.date = parseDateFromUrl(this.activatedRoute);
        this.changeRef.detectChanges();

        const event = await this.recordService.findAllFullByRepositoryId({
          where: {
            repository: {
              id: this.appConfigService.customProperties.eventRepositoryId as any
            },
            id: {
              eq: params.eventId
            }
          },
          cursor: {
            page: 0
          }
        });
        console.log('event', event);
        this.event = event[0];
        this.loading = false;
        // this.pageService.setMetaTags();
        this.changeRef.detectChanges();
      })
    )
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  downloadIcs() {

    // <link rel="alternate" type="text/calendar" title="Event Calendar" href="path/to/your-calendar.ics">
    const calendarEntry = `BEGIN:VCALENDAR
    VERSION:2.0
    PRODID:EVENTFROG//EVENT//ICAL
    BEGIN:VEVENT
    UID:${this.event.id}@wenndenn.ch
    DTSTAMP:20241025T065542Z
    DTSTART:20241031T190000Z
    DTEND:20241031T201500Z
    SUMMARY:QUELLMUND
    DESCRIPTION:Das Mondrian Ensemble und Oboistin Andrea Bischoff präsentieren ein Konzert mit Mozarts Oboenquartett und zeitgenössischen Werken, die die Oboe in den Mittelpunkt stellen.\n\nhttps://eventfrog.ch/de/p/klassik-opern/klassik/quellmund-7237354676194050770.html
      LOCATION:Gare du Nord – Bahnhof für Neue Musik
    GEO:47.567389;7.607131
    URL:${this.event.url}
      END:VEVENT
    END:VCALENDAR`;

    const element = document.createElement('a');
    element.setAttribute('href', 'data:text/calendar;charset=UTF-8,' + encodeURIComponent(calendarEntry));
    element.setAttribute('download', 'foo.ics');

    element.style.display = 'none';
    document.body.appendChild(element);

    element.click();

    document.body.removeChild(element);
  }
}
