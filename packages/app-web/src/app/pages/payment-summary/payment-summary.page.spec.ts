import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { PaymentSummaryPage } from './payment-summary.page';
import { PaymentSummaryPageModule } from './payment-summary.module';
import { AppTestModule, mockPlans } from '../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';
import { AppConfigService } from '../../services/app-config.service';

describe('CheckoutPage', () => {
  let component: PaymentSummaryPage;
  let fixture: ComponentFixture<PaymentSummaryPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        PaymentSummaryPageModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockPlans(apolloMockController);
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    const appConfigService = TestBed.inject(AppConfigService);
    appConfigService.getProductConfigs = () => Promise.resolve([]);

    fixture = TestBed.createComponent(PaymentSummaryPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
