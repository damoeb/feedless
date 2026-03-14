import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { AppTestModule } from '@feedless/testing';
import { SubscriptionsPage } from './subscriptions.page';

describe('AboutUsPage', () => {
  let component: SubscriptionsPage;
  let fixture: ComponentFixture<SubscriptionsPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [SubscriptionsPage, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(SubscriptionsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
