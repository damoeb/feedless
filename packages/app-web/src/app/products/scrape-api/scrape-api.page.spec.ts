import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ScrapeApiPage } from './scrape-api.page';
import { AppTestModule, mockScrape } from '../../app-test.module';
import { ScrapeApiPageModule } from './scrape-api.module';
import { RouterTestingModule } from '@angular/router/testing';

describe('RssBuilderPage', () => {
  let component: ScrapeApiPage;
  let fixture: ComponentFixture<ScrapeApiPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        ScrapeApiPageModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockScrape(apolloMockController);
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ScrapeApiPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
