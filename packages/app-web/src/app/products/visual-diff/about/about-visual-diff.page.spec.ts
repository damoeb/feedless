import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { AboutVisualDiffPage } from './about-visual-diff.page';
import { AppTestModule } from '../../../app-test.module';
import { RssBuilderPageModule } from '../feedless.module';

describe('FeedBuilderPage', () => {
  let component: AboutVisualDiffPage;
  let fixture: ComponentFixture<AboutVisualDiffPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [AboutVisualDiffPage],
      imports: [RssBuilderPageModule, AppTestModule.withDefaults()]
    }).compileComponents();

    fixture = TestBed.createComponent(AboutVisualDiffPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
