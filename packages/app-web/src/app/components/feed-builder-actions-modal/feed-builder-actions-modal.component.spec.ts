import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FeedBuilderActionsModalComponent } from './feed-builder-actions-modal.component';
import { AppTestModule } from '../../app-test.module';
import { RssBuilderProductModule } from '../../products/rss-builder/rss-builder-product.module';

describe('FeedBuilderActionsModal', () => {
  let component: FeedBuilderActionsModalComponent;
  let fixture: ComponentFixture<FeedBuilderActionsModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [FeedBuilderActionsModalComponent],
      imports: [RssBuilderProductModule, AppTestModule.withDefaults()]
    }).compileComponents();

    fixture = TestBed.createComponent(FeedBuilderActionsModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
