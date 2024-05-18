import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { FeedDetailsComponent } from './feed-details.component';
import { FeedDetailsModule } from './feed-details.module';
import {
  AppTestModule,
  mockDocuments,
  mockPlugins,
} from '../../app-test.module';

describe('FeedDetailsComponent', () => {
  let component: FeedDetailsComponent;
  let fixture: ComponentFixture<FeedDetailsComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        FeedDetailsModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockPlugins(apolloMockController);
          mockDocuments(apolloMockController);
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedDetailsComponent);
    component = fixture.componentInstance;
    component.repository = { retention: {}, sources: [], plugins: [] } as any;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
