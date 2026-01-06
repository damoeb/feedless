import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { PricingComponent } from './pricing.component';
import { AppTestModule, mockPlans, mockProducts } from '@feedless/test';
import { RouterTestingModule } from '@angular/router/testing';
import { AppConfigService } from '@feedless/services';
import { GqlVertical } from '@feedless/graphql-api';

describe('PricingComponent', () => {
  let component: PricingComponent;
  let fixture: ComponentFixture<PricingComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        PricingComponent,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) => {
            mockProducts(apolloMockController);
            mockPlans(apolloMockController);
          },
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    const appConfigService = TestBed.inject(AppConfigService);
    appConfigService.getAllAppConfigs = () => Promise.resolve([]);

    fixture = TestBed.createComponent(PricingComponent);
    component = fixture.componentInstance;
    const componentRef = fixture.componentRef;
    componentRef.setInput('vertical', GqlVertical.RssProxy);

    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
