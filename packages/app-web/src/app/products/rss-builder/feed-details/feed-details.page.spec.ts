import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FeedDetailsPage } from './feed-details.page';
import { AppTestModule } from '../../../app-test.module';
import { VisualDiffProductModule } from '../visual-diff-product.module';

describe('VisualDiffPage', () => {
  let component: FeedDetailsPage;
  let fixture: ComponentFixture<FeedDetailsPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [FeedDetailsPage],
      imports: [VisualDiffProductModule, AppTestModule.withDefaults()]
    }).compileComponents();

    fixture = TestBed.createComponent(FeedDetailsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
