import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ProductsPage } from './products.page';
import { ProductsPageModule } from './products.module';
import { AppTestModule } from '../../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';
import { AppConfigService } from '../../../services/app-config.service';

describe('ProductsPage', () => {
  let component: ProductsPage;
  let fixture: ComponentFixture<ProductsPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        ProductsPageModule,
        AppTestModule.withDefaults(),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    const appConfigService = TestBed.inject(AppConfigService);
    appConfigService.getProductConfigs = () => Promise.resolve([]);

    fixture = TestBed.createComponent(ProductsPage);
    component = fixture.componentInstance;
    component.videoUrl = '';
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
