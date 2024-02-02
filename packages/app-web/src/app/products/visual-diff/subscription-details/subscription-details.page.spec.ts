import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { SubscriptionDetailsPage } from './subscription-details.page';
import { AppTestModule } from '../../../app-test.module';
import { SubscriptionDetailsPageModule } from './subscription-details.module';

describe('SubscriptionDetailsPage', () => {
  let component: SubscriptionDetailsPage;
  let fixture: ComponentFixture<SubscriptionDetailsPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [SubscriptionDetailsPageModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(SubscriptionDetailsPage);
    component = fixture.componentInstance;
    component.subscription = {} as any;
    component.documents = [];
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
