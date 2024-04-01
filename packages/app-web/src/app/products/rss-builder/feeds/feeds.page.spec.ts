import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FeedsPage } from './feeds.page';
import { AppTestModule } from '../../../app-test.module';
import { FeedsPageModule } from './feeds.module';

describe('FeedDetailsPage', () => {
  let component: FeedsPage;
  let fixture: ComponentFixture<FeedsPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [FeedsPageModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedsPage);
    component = fixture.componentInstance;
    component.feeds = [];
    component.documents = [];
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
