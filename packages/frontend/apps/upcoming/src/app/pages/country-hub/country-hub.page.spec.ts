import { TestBed } from '@angular/core/testing';
import { AppTestModule } from '@feedless/testing';
import { CountryHubPage } from './country-hub.page';

describe('CountryHubPage', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CountryHubPage, AppTestModule.withDefaults()],
    }).compileComponents();
  });

  it('lists every canton with its place count', () => {
    const fixture = TestBed.createComponent(CountryHubPage);
    fixture.detectChanges();
    const regions = fixture.componentInstance.regions;

    expect(regions.length).toBeGreaterThan(0);
    expect(regions.map((r) => r.area)).toContain('ZG');
    expect(regions.every((r) => r.placeCount > 0)).toBe(true);
  });

  it('sorts the cantons alphabetically', () => {
    const fixture = TestBed.createComponent(CountryHubPage);
    fixture.detectChanges();
    const areas = fixture.componentInstance.regions.map((r) => r.area);

    expect(areas).toEqual([...areas].sort());
  });

  it('renders an h1 and one link per canton', () => {
    const fixture = TestBed.createComponent(CountryHubPage);
    fixture.detectChanges();
    const element: HTMLElement = fixture.nativeElement;

    expect(element.querySelector('h1')?.textContent).toContain('Schweiz');
    expect(element.querySelectorAll('a[href^="/events/in/CH/"]').length).toBe(
      fixture.componentInstance.regions.length,
    );
  });

  it('leads with the h1 before any deeper heading', () => {
    const fixture = TestBed.createComponent(CountryHubPage);
    fixture.detectChanges();
    const headings = Array.from(
      (fixture.nativeElement as HTMLElement).querySelectorAll(
        'h1, h2, h3, h4, h5, h6',
      ),
    ).map((element) => element.tagName.toLowerCase());

    expect(headings[0]).toBe('h1');
  });
});
