import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { AboutRssBuilderPage } from './about-rss-builder.page';
import { AppTestModule } from '../../../app-test.module';
import { AboutRssBuilderModule } from './about-rss-builder.module';

describe('AboutRssBuilderPage', () => {
  let component: AboutRssBuilderPage;
  let fixture: ComponentFixture<AboutRssBuilderPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [AboutRssBuilderModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(AboutRssBuilderPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
