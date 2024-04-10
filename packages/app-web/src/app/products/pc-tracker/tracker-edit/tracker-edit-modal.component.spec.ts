import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { TrackerEditModalComponent } from './tracker-edit-modal.component';
import { AppTestModule } from '../../../app-test.module';
import { TrackerEditPageModule } from './tracker-edit-modal.module';

describe('FeedDetailsPage', () => {
  let component: TrackerEditModalComponent;
  let fixture: ComponentFixture<TrackerEditModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [TrackerEditPageModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(TrackerEditModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
