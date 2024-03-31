import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FeedsPage } from './feeds.page';
import { AppTestModule } from '../../../app-test.module';
import { FeedDetailsPageModule } from './feeds.module';

describe('FeedDetailsPage', () => {
  let component: FeedsPage;
  let fixture: ComponentFixture<FeedsPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [FeedDetailsPageModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedsPage);
    component = fixture.componentInstance;
    component.subscription = {} as any;
    component.documents = [];
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
