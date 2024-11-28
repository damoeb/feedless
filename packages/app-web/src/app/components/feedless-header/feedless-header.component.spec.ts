import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { FeedlessHeaderComponent } from './feedless-header.component';
import { FeedlessHeaderModule } from './feedless-header.module';
import { AppTestModule } from '../../app-test.module';

describe('FeedlessHeaderComponent', () => {
  let component: FeedlessHeaderComponent;
  let fixture: ComponentFixture<FeedlessHeaderComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [FeedlessHeaderComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedlessHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
