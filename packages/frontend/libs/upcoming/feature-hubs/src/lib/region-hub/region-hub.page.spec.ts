import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { AppTestModule } from '@feedless/testing';
import { RegionHubPage } from './region-hub.page';

describe('RegionHubPage', () => {
  const createFixture = async (region: string) => {
    await TestBed.configureTestingModule({
      imports: [RegionHubPage, AppTestModule.withDefaults()],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { params: { countryCode: 'CH', region } } },
        },
      ],
    }).compileComponents();
    const fixture = TestBed.createComponent(RegionHubPage);
    fixture.detectChanges();
    return fixture;
  };

  afterEach(() => TestBed.resetTestingModule());

  it('lists the places of the canton, alphabetically', async () => {
    const fixture = await createFixture('ZG');
    const places = fixture.componentInstance.places;

    expect(places.length).toBeGreaterThan(0);
    expect(places.map((p) => p.place)).toContain('Zug');
    // localeCompare, nicht die Standardsortierung: sonst landet "Neuägeri"
    // hinter "Neuheim", weil ä einen höheren Codepoint als h hat.
    expect(places.map((p) => p.place)).toEqual(
      [...places.map((p) => p.place)].sort((a, b) => a.localeCompare(b)),
    );
  });

  it('links every place to its place page and renders an h1', async () => {
    const fixture = await createFixture('ZG');
    const element: HTMLElement = fixture.nativeElement;

    expect(element.querySelector('h1')?.textContent).toContain('ZG');
    expect(
      element.querySelectorAll('a[href^="/events/in/CH/ZG/"]').length,
    ).toBe(fixture.componentInstance.places.length);
  });

  it('leads with the h1 before any deeper heading', async () => {
    const fixture = await createFixture('ZG');
    const headings = Array.from(
      (fixture.nativeElement as HTMLElement).querySelectorAll(
        'h1, h2, h3, h4, h5, h6',
      ),
    ).map((element) => element.tagName.toLowerCase());

    expect(headings[0]).toBe('h1');
  });

  it('yields no places for an unknown canton', async () => {
    const fixture = await createFixture('QQ');
    expect(fixture.componentInstance.places).toEqual([]);
  });
});
