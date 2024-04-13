import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { BuyPage } from './buy.page';
import { ProductsPageModule } from './buy.module';
import { AppTestModule, mockPlans } from '../../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';

describe('BuyPage', () => {
  let component: BuyPage;
  let fixture: ComponentFixture<BuyPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ProductsPageModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockPlans(apolloMockController);
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(BuyPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
