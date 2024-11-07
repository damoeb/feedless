import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { EventPage } from './event.page';
import { AppTestModule, mockFullRecords } from '../../../app-test.module';
import { EventPageModule } from './event-page.module';

describe('EventComponent', () => {
  let component: EventPage;
  let fixture: ComponentFixture<EventPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        EventPageModule,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) => {
            mockFullRecords(apolloMockController);
          },
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(EventPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
