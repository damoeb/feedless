import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { SubscriptionSettingsComponent } from './subscription-settings.component';
import { SubscriptionSettingsModule } from './subscription-settings.module';
import { RouterTestingModule } from '@angular/router/testing';
import { ApolloTestingModule } from 'apollo-angular/testing';

describe('SubscriptionSettingsComponent', () => {
  let component: SubscriptionSettingsComponent;
  let fixture: ComponentFixture<SubscriptionSettingsComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [
          SubscriptionSettingsModule,
          RouterTestingModule,
          ApolloTestingModule,
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(SubscriptionSettingsComponent);
      component = fixture.componentInstance;
      component.subscription = { feed: {} } as any;
      component.bucket = {} as any;
      fixture.detectChanges();
    })
  );

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
