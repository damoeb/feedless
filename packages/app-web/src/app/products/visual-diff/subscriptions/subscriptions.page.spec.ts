import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { SubscriptionsPage } from './subscriptions.page';
import { AppTestModule } from '../../../app-test.module';
import { SubscriptionsPageModule } from './subscriptions.module';

describe('SubscriptionsPage', () => {
  let component: SubscriptionsPage;
  let fixture: ComponentFixture<SubscriptionsPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [SubscriptionsPageModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(SubscriptionsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
