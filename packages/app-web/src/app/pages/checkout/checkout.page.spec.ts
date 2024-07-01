import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { CheckoutPage } from './checkout.page';
import { CheckoutPageModule } from './checkout.module';
import { AppTestModule } from '../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';
import { AppConfigService } from '../../services/app-config.service';

describe('CheckoutPage', () => {
  let component: CheckoutPage;
  let fixture: ComponentFixture<CheckoutPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        CheckoutPageModule,
        AppTestModule.withDefaults(),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    const appConfigService = TestBed.inject(AppConfigService);
    appConfigService.getProductConfigs = () => Promise.resolve([]);

    fixture = TestBed.createComponent(CheckoutPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
