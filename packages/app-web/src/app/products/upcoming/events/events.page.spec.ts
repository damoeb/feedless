import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { EventsPage } from './events.page';
import {
  AppTestModule,
  mockEvents,
  mockFullRecords,
  mockRecords,
} from '../../../app-test.module';
import { Router } from '@angular/router';

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
    spyOn(router, 'navigateByUrl').and.returnValue(Promise.resolve(true));
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
