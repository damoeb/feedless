import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { FeedBuilderModalComponent } from './feed-builder-modal.component';
import { FeedBuilderModalModule } from './feed-builder-modal.module';

describe('FeedBuilderModalComponent', () => {
  let component: FeedBuilderModalComponent;
  let fixture: ComponentFixture<FeedBuilderModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [FeedBuilderModalModule],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedBuilderModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
