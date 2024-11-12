import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { PricingPage } from './pricing.page';
import { PricingPageModule } from './pricing.module';
import { AppTestModule } from '../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';
import { AppConfigService } from '../../services/app-config.service';

describe('PricingPage', () => {
  let component: PricingPage;
  let fixture: ComponentFixture<PricingPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        PricingPageModule,
        AppTestModule.withDefaults(),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    const appConfigService = TestBed.inject(AppConfigService);
    appConfigService.getAllAppConfigs = () => Promise.resolve([]);

    fixture = TestBed.createComponent(PricingPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
