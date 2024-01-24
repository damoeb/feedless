import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FeedlessProductPage } from './feedless-product.page';
import { AppTestModule, mockScrape } from '../../app-test.module';
import { RssBuilderPageModule } from './feedless-product.module';
import { RouterTestingModule } from '@angular/router/testing';

describe('RssBuilderPage', () => {
  let component: FeedlessProductPage;
  let fixture: ComponentFixture<FeedlessProductPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        RssBuilderPageModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockScrape(apolloMockController);
        }),
        RouterTestingModule.withRoutes([])
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(FeedlessProductPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
