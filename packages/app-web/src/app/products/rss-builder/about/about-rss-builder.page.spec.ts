import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { AboutRssBuilderPage } from './about-rss-builder.page';
import { AppTestModule } from '../../../app-test.module';
import { RssBuilderProductModule } from '../rss-builder-product.module';

describe('FeedBuilderPage', () => {
  let component: AboutRssBuilderPage;
  let fixture: ComponentFixture<AboutRssBuilderPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [AboutRssBuilderPage],
      imports: [RssBuilderProductModule, AppTestModule.withDefaults()]
    }).compileComponents();

    fixture = TestBed.createComponent(AboutRssBuilderPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
