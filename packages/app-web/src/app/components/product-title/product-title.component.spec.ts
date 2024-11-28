import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ProductTitleComponent } from './product-title.component';
import { AppTestModule } from '../../app-test.module';

describe('ProductTitleComponent', () => {
  let component: ProductTitleComponent;
  let fixture: ComponentFixture<ProductTitleComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        ProductTitleComponent,
        AppTestModule.withDefaults({ mockAppConfig: false }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ProductTitleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
