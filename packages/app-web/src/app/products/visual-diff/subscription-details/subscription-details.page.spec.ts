import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { SubscriptionDetailsPage } from './subscription-details.page';
import { AppTestModule } from '../../../app-test.module';
import { VisualDiffProductModule } from '../visual-diff-product.module';

describe('VisualDiffPage', () => {
  let component: SubscriptionDetailsPage;
  let fixture: ComponentFixture<SubscriptionDetailsPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [SubscriptionDetailsPage],
      imports: [VisualDiffProductModule, AppTestModule.withDefaults()]
    }).compileComponents();

    fixture = TestBed.createComponent(SubscriptionDetailsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
