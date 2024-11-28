import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ProductHeadlineComponent } from './product-headline.component';
import { ProductHeadlineModule } from './product-headline.module';

describe('ProductHeadlineComponent', () => {
  let component: ProductHeadlineComponent;
  let fixture: ComponentFixture<ProductHeadlineComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [ProductHeadlineComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ProductHeadlineComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
