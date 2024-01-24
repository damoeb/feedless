import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ProductTitleComponent } from './product-title.component';
import { ProductTitleModule } from './product-title.module';

describe('BubbleComponent', () => {
  let component: ProductTitleComponent;
  let fixture: ComponentFixture<ProductTitleComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [ProductTitleModule]
    }).compileComponents();

    fixture = TestBed.createComponent(ProductTitleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
