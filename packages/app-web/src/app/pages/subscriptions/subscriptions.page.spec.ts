import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SubscriptionsPage } from './subscriptions.page';
import { ApolloMockController, AppTestModule, mockServerSettings } from '../../app-test.module';
import { ServerConfigService } from '../../services/server-config.service';
import { ApolloClient } from '@apollo/client/core';
import { Plans } from '../../../generated/graphql';

describe('SubscriptionsPage', () => {
  let component: SubscriptionsPage;
  let fixture: ComponentFixture<SubscriptionsPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SubscriptionsPage, AppTestModule.withDefaults()],
      providers: [],
    }).compileComponents();

    const apolloMockController = TestBed.inject(ApolloMockController);

    // Mock the Plans query to return empty array
    apolloMockController.mockQuery(Plans).and.resolveOnce(async () => ({
      data: {
        plans: [],
      },
    }));

    await mockServerSettings(
      apolloMockController,
      TestBed.inject(ServerConfigService),
      TestBed.inject(ApolloClient)
    );

    fixture = TestBed.createComponent(SubscriptionsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
