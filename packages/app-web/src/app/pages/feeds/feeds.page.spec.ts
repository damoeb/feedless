import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { FeedsPage } from './feeds.page';
import { FeedsPageModule } from './feeds.module';
import { AppTestModule, mockSearchNativeFeeds } from '../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';

describe('FeedsPage', () => {
  let component: FeedsPage;
  let fixture: ComponentFixture<FeedsPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        FeedsPageModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockSearchNativeFeeds(apolloMockController);
        }),
        RouterTestingModule,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
