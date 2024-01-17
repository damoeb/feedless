import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RssBuilderPage } from './rss-builder.page';
import { AppTestModule, mockScrape } from '../../app-test.module';
import { RssBuilderPageModule } from './rss-builder.module';
import { RouterTestingModule } from '@angular/router/testing';

describe('RssBuilderPage', () => {
  let component: RssBuilderPage;
  let fixture: ComponentFixture<RssBuilderPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        RssBuilderPageModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockScrape(apolloMockController);
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RssBuilderPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
