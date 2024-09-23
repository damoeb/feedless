import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { PricingComponent } from './pricing.component';
import { PricingModule } from './pricing.module';
import { AppTestModule, mockProducts } from '../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';
import { AppConfigService } from '../../services/app-config.service';
import { GqlProductCategory } from '../../../generated/graphql';

describe('PricingComponent', () => {
  let component: PricingComponent;
  let fixture: ComponentFixture<PricingComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        PricingModule,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) =>
            mockProducts(apolloMockController),
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    const appConfigService = TestBed.inject(AppConfigService);
    appConfigService.getProductConfigs = () => Promise.resolve([]);

    fixture = TestBed.createComponent(PricingComponent);
    component = fixture.componentInstance;
    component.productCategory = GqlProductCategory.RssProxy;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
