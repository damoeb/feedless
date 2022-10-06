import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { SubscriptionsComponent } from './subscriptions.component';
import { SubscriptionsModule } from './subscriptions.module';
import { ApolloTestingModule } from 'apollo-angular/testing';

describe('SubscriptionsComponent', () => {
  let component: SubscriptionsComponent;
  let fixture: ComponentFixture<SubscriptionsComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [SubscriptionsModule, ApolloTestingModule],
      }).compileComponents();

      fixture = TestBed.createComponent(SubscriptionsComponent);
      component = fixture.componentInstance;
      component.bucket = {} as any;
      fixture.detectChanges();
    })
  );

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
