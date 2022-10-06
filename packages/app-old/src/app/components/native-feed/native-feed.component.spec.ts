import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { NativeFeedComponent } from './native-feed.component';
import { NativeFeedModule } from './native-feed.module';
import { ApolloTestingModule } from 'apollo-angular/testing';

describe('NativeFeedModule', () => {
  let component: NativeFeedComponent;
  let fixture: ComponentFixture<NativeFeedComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [NativeFeedModule, ApolloTestingModule],
      }).compileComponents();

      fixture = TestBed.createComponent(NativeFeedComponent);
      component = fixture.componentInstance;
      component.feed = {} as any;
      fixture.detectChanges();
    })
  );

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
