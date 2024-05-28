import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { SubscriptionEditPage } from './subscription-edit.page';
import {
  ApolloMockController,
  AppTestModule,
  mockServerSettings,
} from '../../../app-test.module';
import { SubscriptionEditPageModule } from './subscription-edit.module';
import { ServerConfigService } from '../../../services/server-config.service';
import { ApolloClient } from '@apollo/client/core';

describe('SubscriptionEditPage', () => {
  let component: SubscriptionEditPage;
  let fixture: ComponentFixture<SubscriptionEditPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [SubscriptionEditPageModule, AppTestModule.withDefaults()],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerConfigService),
      TestBed.inject(ApolloClient),
    );

    fixture = TestBed.createComponent(SubscriptionEditPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
