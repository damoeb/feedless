import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { EventPageComponent } from './event-page.component';
import { AppTestModule } from '../../../app-test.module';
import { EventPageModule } from './event-page.module';

describe('EventComponent', () => {
  let component: EventPageComponent;
  let fixture: ComponentFixture<EventPageComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [EventPageModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(EventPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
