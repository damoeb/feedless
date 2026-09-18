import { ComponentFixture, TestBed } from '@angular/core/testing';
import { InlineCalendarComponent } from './inline-calendar.component';
import dayjs from 'dayjs';

describe('InlineCalendar', () => {
  let component: InlineCalendarComponent;
  let fixture: ComponentFixture<InlineCalendarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InlineCalendarComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(InlineCalendarComponent);
    // const router = TestBed.inject(Router);
    // vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    component = fixture.componentInstance;
    // fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('#getSeoLinkAttributes', () => {
    it('marks today as indexable, because it is the canonical place page', () => {
      const attrs = component.getSeoLinkAttributes(dayjs());
      expect(attrs.split(' ')).toContain('index');
      expect(attrs.split(' ')).not.toContain('noindex');
    });

    [-1, 1, 6, 10].forEach((offset) => {
      it(`marks an offset of ${offset} days as noindex, it is only a ?date= view`, () => {
        const attrs = component.getSeoLinkAttributes(
          dayjs().startOf('day').add(offset, 'day'),
        );
        expect(attrs.split(' ')).toContain('noindex');
        expect(attrs.split(' ')).not.toContain('index');
      });
    });
  });
});
