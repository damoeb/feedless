import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProductsPage } from './products.page';
import { AppTestModule } from '../../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';
import { AppConfigService } from '../../../services/app-config.service';

describe('ProductsPage', () => {
  let component: ProductsPage;
  let fixture: ComponentFixture<ProductsPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ProductsPage,
        AppTestModule.withDefaults(),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    const appConfigService = TestBed.inject(AppConfigService);
    appConfigService.getAllAppConfigs = () => Promise.resolve([]);

    fixture = TestBed.createComponent(ProductsPage);
    component = fixture.componentInstance;
    component.videoUrl = '';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
