import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { UpcomingProductPage } from './upcoming-product-page.component';
import { AppTestModule, mockScrape } from '../../app-test.module';
import { UpcomingProductModule } from './upcoming-product.module';
import { RouterTestingModule } from '@angular/router/testing';

describe('ReaderPage', () => {
  let component: UpcomingProductPage;
  let fixture: ComponentFixture<UpcomingProductPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        UpcomingProductModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockScrape(apolloMockController);
        }),
        RouterTestingModule.withRoutes([])
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(UpcomingProductPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
