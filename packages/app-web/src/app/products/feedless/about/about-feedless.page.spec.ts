import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { AboutFeedlessPage } from './about-feedless.page';
import { AppTestModule } from '../../../app-test.module';
import { RssBuilderPageModule } from '../feedless-product.module';

describe('FeedBuilderPage', () => {
  let component: AboutFeedlessPage;
  let fixture: ComponentFixture<AboutFeedlessPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [AboutFeedlessPage],
      imports: [RssBuilderPageModule, AppTestModule.withDefaults()]
    }).compileComponents();

    fixture = TestBed.createComponent(AboutFeedlessPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
