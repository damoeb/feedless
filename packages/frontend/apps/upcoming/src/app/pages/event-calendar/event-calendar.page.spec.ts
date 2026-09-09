import { ComponentFixture, TestBed } from '@angular/core/testing';
import type { MockedObject } from 'vitest';
import { EventCalendarPage } from './event-calendar.page';
import {
  AppTestModule,
  mockEvents,
  mockFullRecords,
  mockRecords,
} from '@feedless/testing';
import { Router } from '@angular/router';
import dayjs from 'dayjs';
import { EventService } from '../../event.service';
// eslint-disable-next-line @nx/enforce-module-boundaries
import { AppConfigService } from '@feedless/components';
import { of } from 'rxjs';

describe('EventCalendarPage', () => {
  let component: EventCalendarPage;
  let fixture: ComponentFixture<EventCalendarPage>;
  let eventService: EventService;
  let appConfigService: AppConfigService;
  let eventRepositoryId: string;

  beforeEach(async () => {
    eventService = {
      fetchEventsBetweenDates: vi.fn().mockReturnValue([]),
      findAllByRepositoryId: vi.fn().mockReturnValue(Promise.resolve([])),
    } as any as MockedObject<EventService>;
    eventRepositoryId = Math.random().toString(16);

    appConfigService = {
      customProperties: {
        eventRepositoryId,
      },
      getActiveProductConfigChange: () => of({}),
    } as any as MockedObject<AppConfigService>;

    await TestBed.configureTestingModule({
      providers: [
        {
          provide: EventService,
          useValue: eventService,
        },
        {
          provide: AppConfigService,
          useValue: appConfigService,
        },
      ],
      imports: [
        EventCalendarPage,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) => {
            mockEvents(apolloMockController);
            mockRecords(apolloMockController);
            mockFullRecords(apolloMockController);
          },
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(EventCalendarPage);
    const router = TestBed.inject(Router);
    vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    component = fixture.componentInstance;

    component.date = dayjs();
    component.perimeter = 10;

    await component.ngOnInit();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('#cleanTitle', () => {
    expect(component.cleanTitle('02.10.202502.10.2025 Mittagessen')).toEqual(
      'Mittagessen',
    );
  });

  describe('rendered markup', () => {
    const zug = {
      lat: 47.1679898,
      lng: 8.5173652,
      place: 'Zug',
      displayName: '6300, Zug',
      area: 'ZG',
      countryCode: 'CH',
    };

    beforeEach(() => {
      const today = dayjs();
      component.namedLatLon = zug;
      component.loadingDay = false;
      component.placesByDistancePerDay = [
        {
          date: today,
          eventGroups: [
            {
              distance: 0,
              places: [
                {
                  place: zug,
                  events: [
                    {
                      id: '1',
                      title: 'Konzert im Burggarten',
                      url: 'https://example.com/event1',
                      startingAt: today.hour(19).minute(30).valueOf(),
                      latLng: { lat: zug.lat, lng: zug.lng },
                      text: 'Ein Abend mit Musik',
                    },
                  ],
                },
              ],
            },
          ],
        },
      ] as any;
      // OnPush: eine direkte Feldzuweisung markiert die View nicht als dirty,
      // und fixture.changeDetectorRef ist die Host-View, nicht die der
      // Komponente.
      fixture.componentRef.changeDetectorRef.markForCheck();
      (component as any).changeRef.detectChanges();
      fixture.detectChanges();
    });

    const headings = (): string[] =>
      Array.from(
        (fixture.nativeElement as HTMLElement).querySelectorAll(
          'h1, h2, h3, h4, h5, h6',
        ),
      ).map((element) => element.tagName.toLowerCase());

    it('leads with the h1 before any deeper heading', () => {
      expect(headings().length).toBeGreaterThan(1);
      expect(headings()[0]).toBe('h1');
    });

    it('carries exactly one h1', () => {
      expect(headings().filter((tag) => tag === 'h1').length).toBe(1);
    });

    it('renders no elements hidden from users but shown to crawlers', () => {
      expect(
        (fixture.nativeElement as HTMLElement).querySelectorAll('.bot-only')
          .length,
      ).toBe(0);
    });

    it('renders the start time as visible text', () => {
      const time = (fixture.nativeElement as HTMLElement).querySelector(
        '.event-details time[datetime]',
      );
      expect(time?.textContent).toContain('19:30');
    });
  });

  describe('#formatTime', () => {
    it('formats a time of day', () => {
      expect(component.formatTime(dayjs().hour(19).minute(30).valueOf())).toBe(
        '19:30',
      );
    });

    it('yields an empty string for midnight, which marks an all day entry', () => {
      expect(
        component.formatTime(dayjs().startOf('day').valueOf()),
      ).toBe('');
    });
  });

  // it('should have rel="nofollow" attribute on event links when rendered', () => {
  //   // Set up component with mock data
  //   component.location = {
  //     lat: 48.1351,
  //     lng: 11.582,
  //     place: 'München',
  //     displayName: 'München',
  //     area: 'Bayern',
  //     countryCode: 'DE',
  //   };
  //
  //   // Mock events data
  //   const mockEvents = [
  //     {
  //       id: '1',
  //       title: 'Test Event 1',
  //       url: 'https://example.com/event1',
  //       startingAt: dayjs().add(1, 'day').toISOString(),
  //       latLng: { lat: 48.1351, lng: 11.582 },
  //       text: 'Test event description',
  //     },
  //     {
  //       id: '2',
  //       title: 'Test Event 2',
  //       url: 'https://example.com/event2',
  //       startingAt: dayjs().add(2, 'days').toISOString(),
  //       latLng: { lat: 48.1351, lng: 11.582 },
  //       text: 'Another test event',
  //     },
  //   ];
  //
  //   const today = dayjs();
  //   component.placesByDistancePerDay = [
  //     {
  //       date: today,
  //       eventGroups: [
  //         {
  //           distance: 0,
  //           places: [
  //             {
  //               place: component.location,
  //               events: mockEvents,
  //             },
  //           ],
  //         },
  //       ],
  //     },
  //   ];
  //
  //   component.date = today;
  //   component.loadingDay = false;
  //   fixture.detectChanges();
  //
  //   // Test that the component has the correct structure and data
  //   expect(component.location).toBeTruthy();
  //   expect(component.date).toBeTruthy();
  //   expect(component.loadingDay).toBe(false);
  //   expect(component.placesByDistancePerDay.length).toBeGreaterThan(0);
  //   expect(
  //     component.placesByDistancePerDay[0].eventGroups[0].places[0].events
  //       .length,
  //   ).toBe(2);
  //
  //   // Test that the events have external URLs
  //   const events =
  //     component.placesByDistancePerDay[0].eventGroups[0].places[0].events;
  //   events.forEach((event) => {
  //     expect(event.url).toMatch(/^https?:\/\//);
  //   });
  //
  //   // Test that the component renders without errors
  //   expect(fixture.debugElement.nativeElement).toBeTruthy();
  // });
});
