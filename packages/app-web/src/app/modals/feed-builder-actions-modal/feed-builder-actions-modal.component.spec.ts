import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FeedBuilderActionsModalComponent } from './feed-builder-actions-modal.component';
import { AppTestModule } from '../../app-test.module';
import { FeedBuilderActionsModalModule } from './feed-builder-actions-modal.module';

describe('FeedBuilderActionsModalComponent', () => {
  let component: FeedBuilderActionsModalComponent;
  let fixture: ComponentFixture<FeedBuilderActionsModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [FeedBuilderActionsModalModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedBuilderActionsModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
