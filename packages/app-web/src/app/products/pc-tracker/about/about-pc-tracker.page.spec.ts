import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { AboutPcTrackerPage } from './about-pc-tracker.page';
import { AppTestModule } from '../../../app-test.module';
import { AboutPcTrackerModule } from './about-pc-tracker.module';

describe('AboutPcTrackerPage', () => {
  let component: AboutPcTrackerPage;
  let fixture: ComponentFixture<AboutPcTrackerPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [AboutPcTrackerModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(AboutPcTrackerPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
