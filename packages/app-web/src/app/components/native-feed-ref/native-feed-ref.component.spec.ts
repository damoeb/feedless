import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { NativeFeedRefComponent } from './native-feed-ref.component';
import { NativeFeedRefModule } from './native-feed-ref.module';
import { AppTestModule } from '../../app-test.module';
import { BasicNativeFeed } from '../../graphql/types';

describe('NativeFeedRefComponent', () => {
  let component: NativeFeedRefComponent;
  let fixture: ComponentFixture<NativeFeedRefComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [NativeFeedRefModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(NativeFeedRefComponent);
    component = fixture.componentInstance;
    component.feed = {} as BasicNativeFeed;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
