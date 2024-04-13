import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { TrackerEditModalComponent } from './tracker-edit-modal.component';
import { AppTestModule } from '../../../app-test.module';
import { TrackerEditModalModule } from './tracker-edit-modal.module';

describe('TrackerEditModalComponent', () => {
  let component: TrackerEditModalComponent;
  let fixture: ComponentFixture<TrackerEditModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [TrackerEditModalModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(TrackerEditModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
