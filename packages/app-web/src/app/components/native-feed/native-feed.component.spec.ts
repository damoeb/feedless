import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { NativeFeedComponent } from './native-feed.component';
import { NativeFeedModule } from './native-feed.module';
import {
  AppTestModule,
  mockNativeFeedById,
  mockSearchArticles,
} from '../../app-test.module';

describe('NativeFeedComponent', () => {
  let component: NativeFeedComponent;
  let fixture: ComponentFixture<NativeFeedComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        NativeFeedModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockNativeFeedById(apolloMockController);
          mockSearchArticles(apolloMockController);
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(NativeFeedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
