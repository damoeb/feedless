import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { InlineCalendarComponent } from './inline-calendar.component';
import { relativeDateIncrement } from '../../upcoming-product-routes';
import dayjs from 'dayjs';

describe('InlineCalendar', () => {
  let component: InlineCalendarComponent;
  let fixture: ComponentFixture<InlineCalendarComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [InlineCalendarComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(InlineCalendarComponent);
    // const router = TestBed.inject(Router);
    // jest.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    component = fixture.componentInstance;
    // fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('#getSeoLinkAttributes', () => {
    it('relative dates return index=true', () => {
      Object.values(relativeDateIncrement).forEach((increment) => {
        const attrs = component.getSeoLinkAttributes(
          dayjs().add(increment, 'day'),
        );
        expect(attrs.indexOf('index') > -1).toBe(true);
        expect(attrs.indexOf('no-index') === -1).toBe(true);
      });
    });
    it('other dates return index=false', () => {
      Object.values(relativeDateIncrement).forEach((increment) => {
        const attrs = component.getSeoLinkAttributes(
          dayjs().add(increment, 'day'),
        );
        expect(attrs.indexOf('no-index') > -1).toBe(true);
        expect(attrs.indexOf('index') === -1).toBe(true);
      });
    });
  });
});
