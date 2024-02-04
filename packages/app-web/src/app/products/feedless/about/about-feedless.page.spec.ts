import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { AboutFeedlessPage } from './about-feedless.page';
import { AppTestModule } from '../../../app-test.module';
import { AboutFeedlessModule } from './about-feedless.module';

describe('AboutFeedlessPage', () => {
  let component: AboutFeedlessPage;
  let fixture: ComponentFixture<AboutFeedlessPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [AboutFeedlessModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(AboutFeedlessPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});