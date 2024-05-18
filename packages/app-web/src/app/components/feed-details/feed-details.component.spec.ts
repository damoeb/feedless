import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { FeedDetailsComponent } from './feed-details.component';
import { FeedDetailsModule } from './feed-details.module';
import { AppTestModule } from '../../app-test.module';

describe('FeedDetailsComponent', () => {
  let component: FeedDetailsComponent;
  let fixture: ComponentFixture<FeedDetailsComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [FeedDetailsModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
