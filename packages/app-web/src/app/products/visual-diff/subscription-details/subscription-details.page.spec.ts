import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { SubscriptionDetailsPage } from './subscription-details.page';
import { AppTestModule } from '../../../app-test.module';
import { VisualDiffPageModule } from '../visual-diff.module';

describe('VisualDiffPage', () => {
  let component: SubscriptionDetailsPage;
  let fixture: ComponentFixture<SubscriptionDetailsPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [SubscriptionDetailsPage],
      imports: [VisualDiffPageModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(SubscriptionDetailsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
