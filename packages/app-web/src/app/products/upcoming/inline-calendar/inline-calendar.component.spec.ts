import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { InlineCalendarComponent } from './inline-calendar.component';
import {
  AppTestModule,
  mockEvents,
  mockFullRecords,
  mockRecords,
} from '../../../app-test.module';
import { Router } from '@angular/router';

describe('EventsPage', () => {
  let component: InlineCalendarComponent;
  let fixture: ComponentFixture<InlineCalendarComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        InlineCalendarComponent,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) => {
            mockEvents(apolloMockController);
            mockRecords(apolloMockController);
            mockFullRecords(apolloMockController);
          },
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(InlineCalendarComponent);
    const router = TestBed.inject(Router);
    jest.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
