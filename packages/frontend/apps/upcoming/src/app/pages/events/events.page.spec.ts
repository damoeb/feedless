import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { EventsPage } from './events.page';
import {
  AppTestModule,
  mockEvents,
  mockFullRecords,
  mockRecords,
} from '@feedless/testing';
import { Router } from '@angular/router';
import dayjs from 'dayjs';
import { EventService } from '../../event.service';
import { AppConfigService } from '@feedless/components';
import { of } from 'rxjs';
import { LatLng } from '@feedless/core';

describe('EventsPage', () => {
  let component: EventsPage;
  let fixture: ComponentFixture<EventsPage>;
  let eventService: EventService;
  let appConfigService: AppConfigService;
  let eventRepositoryId: string;

  beforeEach(waitForAsync(async () => {
    eventService = {
      fetchEventsBetweenDates: jest.fn().mockReturnValue([]),
      findAllByRepositoryId: jest.fn().mockReturnValue(Promise.resolve([])),
    } as any as jest.Mocked<EventService>;
    eventRepositoryId = Math.random().toString(16);

    appConfigService = {
      customProperties: {
        eventRepositoryId,
      },
      getActiveProductConfigChange: () => of({}),
    } as any as jest.Mocked<AppConfigService>;

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
        EventsPage,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) => {
            mockEvents(apolloMockController);
            mockRecords(apolloMockController);
            mockFullRecords(apolloMockController);
          },
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(EventsPage);
    const router = TestBed.inject(Router);
    jest.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    component = fixture.componentInstance;

    component.date = dayjs();
    component.perimeter = 10;

    await component.ngOnInit();
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('#cleanTitle', () => {
    expect(component.cleanTitle('02.10.202502.10.2025 Mittagessen')).toEqual(
      ' Mittagessen',
    );
  });

  it('#fetchEventsBetweenDates uses minDate start and maxDate end', () => {
    // given
    const date = dayjs();
    const latLon: LatLng = { lat: Math.random(), lng: Math.random() };
    component.latLon = latLon;

    // when
    component.fetchEventsBetweenDates(date, date);

    // then
    expect(eventService.findAllByRepositoryId).toHaveBeenCalledWith({
      cursor: {
        page: 0,
        pageSize: 50,
      },
      where: {
        repository: {
          id: eventRepositoryId,
        },
        latLng: {
          near: {
            point: {
              lat: latLon.lat,
              lng: latLon.lng,
            },
            distanceKm: 10,
          },
        },
        startedAt: {
          after: date.startOf('day').valueOf(),
          before: date.endOf('day').valueOf(),
        },
      },
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
