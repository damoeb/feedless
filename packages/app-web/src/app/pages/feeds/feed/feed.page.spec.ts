import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { FeedPage } from './feed.page';
import { FeedPageModule } from './feed.module';
import {
  AppTestModule,
  mockNativeFeedById,
  mockSearchArticles,
} from '../../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';

describe('FeedPage', () => {
  let component: FeedPage;
  let fixture: ComponentFixture<FeedPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        FeedPageModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockNativeFeedById(apolloMockController);
          mockSearchArticles(apolloMockController);
        }),
        RouterTestingModule,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
