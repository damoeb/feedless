import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { PricingComponent } from './pricing.component';
import { AppTestModule, mockProducts } from '../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';
import { AppConfigService } from '../../services/app-config.service';
import { GqlVertical } from '../../../generated/graphql';

describe('PricingComponent', () => {
  let component: PricingComponent;
  let fixture: ComponentFixture<PricingComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        PricingComponent,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) =>
            mockProducts(apolloMockController),
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    const appConfigService = TestBed.inject(AppConfigService);
    appConfigService.getAllAppConfigs = () => Promise.resolve([]);

    fixture = TestBed.createComponent(PricingComponent);
    component = fixture.componentInstance;
    component.vertical = GqlVertical.RssProxy;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
