import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { PaymentPage } from './payment.page';
import { PaymentPageModule } from './payment.module';
import { AppTestModule, mockPlans } from '../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';
import { AppConfigService } from '../../services/app-config.service';

describe('CheckoutPage', () => {
  let component: PaymentPage;
  let fixture: ComponentFixture<PaymentPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        PaymentPageModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockPlans(apolloMockController);
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    const appConfigService = TestBed.inject(AppConfigService);
    appConfigService.getProductConfigs = () => Promise.resolve([]);

    fixture = TestBed.createComponent(PaymentPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
