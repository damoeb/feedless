import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SubscriptionsPage } from './subscriptions.page';
import {
  ApolloMockController,
  AppTestModule,
  mockServerSettings,
} from '../../app-test.module';
import { ServerConfigService } from '../../services/server-config.service';
import { ApolloClient } from '@apollo/client/core';

describe('SubscriptionsPage', () => {
  let component: SubscriptionsPage;
  let fixture: ComponentFixture<SubscriptionsPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SubscriptionsPage, AppTestModule.withDefaults()],
      providers: [],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerConfigService),
      TestBed.inject(ApolloClient),
    );

    fixture = TestBed.createComponent(SubscriptionsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
