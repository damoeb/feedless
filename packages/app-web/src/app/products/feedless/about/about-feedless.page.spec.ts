import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { AboutFeedlessPage } from './about-feedless.page';
import { AppTestModule } from '../../../app-test.module';
import { AboutFeedlessModule } from './about-feedless.module';
import { ProductService } from '../../../services/product.service';

describe('AboutFeedlessPage', () => {
  let component: AboutFeedlessPage;
  let fixture: ComponentFixture<AboutFeedlessPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [AboutFeedlessModule, AppTestModule.withDefaults()],
    }).compileComponents();

    const productService = TestBed.inject(ProductService);
    productService.getProductConfigs = () => Promise.resolve([]);

    fixture = TestBed.createComponent(AboutFeedlessPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
