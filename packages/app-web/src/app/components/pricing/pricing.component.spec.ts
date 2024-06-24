import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { PricingComponent } from './pricing.component';
import { PricingModule } from './pricing.module';
import { AppTestModule, mockPlans } from '../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';
import { AppConfigService } from '../../services/app-config.service';

describe('PricingComponent', () => {
  let component: PricingComponent;
  let fixture: ComponentFixture<PricingComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        PricingModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockPlans(apolloMockController);
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    const appConfigService = TestBed.inject(AppConfigService);
    appConfigService.getProductConfigs = () => Promise.resolve([]);

    fixture = TestBed.createComponent(PricingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
