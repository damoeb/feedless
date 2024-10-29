import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { EventsPageComponent } from './events-page.component';
import { AppTestModule } from '../../../app-test.module';
import { EventsPageModule } from './events-page.module';

describe('EventsComponent', () => {
  let component: EventsPageComponent;
  let fixture: ComponentFixture<EventsPageComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [EventsPageModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(EventsPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
