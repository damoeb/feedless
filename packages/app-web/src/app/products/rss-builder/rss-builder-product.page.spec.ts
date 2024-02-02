import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RssBuilderProductPage } from './rss-builder-product.page';
import { AppTestModule, mockScrape } from '../../app-test.module';
import { RssBuilderProductModule } from './rss-builder-product.module';
import { RouterTestingModule } from '@angular/router/testing';

describe('RssBuilderProductPage', () => {
  let component: RssBuilderProductPage;
  let fixture: ComponentFixture<RssBuilderProductPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        RssBuilderProductModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockScrape(apolloMockController);
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RssBuilderProductPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
