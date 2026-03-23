import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { AppTestModule } from '@feedless/testing';
import { FeedSubscriptionsPage } from './feed-subscriptions.page';

describe('FeedSubscriptionsPage', () => {
  let component: FeedSubscriptionsPage;
  let fixture: ComponentFixture<FeedSubscriptionsPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [FeedSubscriptionsPage, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedSubscriptionsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
