import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { EventsPage } from './events.page';
import {
  AppTestModule,
  mockEvents,
  mockFullRecords,
  mockRecords,
} from '../../../app-test.module';
import { Router } from '@angular/router';
import dayjs from 'dayjs';

describe('EventsPage', () => {
  let component: EventsPage;
  let fixture: ComponentFixture<EventsPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
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
});
