import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ProductHeadlineComponent } from './product-headline.component';

describe('ProductHeadlineComponent', () => {
  let component: ProductHeadlineComponent;
  let fixture: ComponentFixture<ProductHeadlineComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductHeadlineComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ProductHeadlineComponent);
    component = fixture.componentInstance;
    const componentRef = fixture.componentRef;
    componentRef.setInput('title', '');
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
