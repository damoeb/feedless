import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { AppTestModule } from '@feedless/testing';
import dayjs from 'dayjs';
import { EventDetailPage } from './event-detail.page';
import { MIN_OWN_TEXT_LENGTH } from './event-content';

describe('EventDetailPage', () => {
  const place = {
    lat: 47.1679898,
    lng: 8.5173652,
    place: 'Zug',
    displayName: '6300, Zug',
    area: 'ZG',
    countryCode: 'CH',
  };

  const richText = `Chilbi Baar ${'x'.repeat(MIN_OWN_TEXT_LENGTH)}`;

  const createFixture = async (eventDetail: unknown) => {
    await TestBed.configureTestingModule({
      imports: [EventDetailPage, AppTestModule.withDefaults()],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { data: { eventDetail }, params: {} } },
        },
      ],
    }).compileComponents();
    const fixture = TestBed.createComponent(EventDetailPage);
    fixture.detectChanges();
    return fixture;
  };

  const eventAt = (startingAt: number, text: string) => ({
    event: { id: 'abc', title: 'Chilbi Baar', text, url: 'https://x.example/1', startingAt },
    place,
  });

  afterEach(() => TestBed.resetTestingModule());

  it('renders the title as the only h1', async () => {
    const fixture = await createFixture(
      eventAt(dayjs().add(1, 'day').valueOf(), richText),
    );
    const element: HTMLElement = fixture.nativeElement;
    const h1 = element.querySelectorAll('h1');

    expect(h1).toHaveLength(1);
    expect(h1[0].textContent).toContain('Chilbi Baar');
  });

  it('links back to the place page and out to the source', async () => {
    const fixture = await createFixture(
      eventAt(dayjs().add(1, 'day').valueOf(), richText),
    );
    const element: HTMLElement = fixture.nativeElement;

    expect(
      element.querySelector('a[href="/events/in/CH/ZG/Zug"]'),
    ).toBeTruthy();
    expect(
      element.querySelector('a[href="https://x.example/1"][rel~="nofollow"]'),
    ).toBeTruthy();
  });

  /**
   * 96 % der Events tragen 10:00 als Default-Startzeit. Eine angezeigte
   * Uhrzeit wäre für fast jedes Event erfunden.
   */
  it('shows a date but never a time', async () => {
    const fixture = await createFixture(
      eventAt(dayjs('2026-09-12T10:00:00').valueOf(), richText),
    );
    const time = (fixture.nativeElement as HTMLElement).querySelector('time');

    expect(time?.getAttribute('datetime')).toBe('2026-09-12');
    expect(time?.textContent).not.toMatch(/\d{1,2}:\d{2}/);
  });

  it('marks an event that already happened', async () => {
    const fixture = await createFixture(
      eventAt(dayjs().subtract(2, 'day').valueOf(), richText),
    );
    expect(fixture.componentInstance.isPast).toBe(true);
    expect((fixture.nativeElement as HTMLElement).textContent).toContain(
      'Vergangenheit',
    );
  });

  it('degrades to a not-found page when nothing resolved', async () => {
    const fixture = await createFixture(null);
    const element: HTMLElement = fixture.nativeElement;

    expect(element.querySelector('h1')?.textContent).toContain(
      'nicht gefunden',
    );
  });
});
