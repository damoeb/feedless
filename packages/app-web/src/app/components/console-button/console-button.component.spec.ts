import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ConsoleButtonComponent } from './console-button.component';
import {
  ApolloMockController,
  AppTestModule,
  mockServerSettings,
} from '../../app-test.module';
import {
  Agents,
  GqlAgentsQuery,
  GqlAgentsQueryVariables,
} from '../../../generated/graphql';
import { ServerConfigService } from '../../services/server-config.service';
import { ApolloClient } from '@apollo/client/core';

describe('ConsoleButtonComponent', () => {
  let component: ConsoleButtonComponent;
  let fixture: ComponentFixture<ConsoleButtonComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ConsoleButtonComponent,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) => {
            apolloMockController
              .mockQuery<GqlAgentsQuery, GqlAgentsQueryVariables>(Agents)
              .and.resolveOnce(async () => {
                return {
                  data: {
                    agents: [],
                  },
                };
              });
          },
        }),
      ],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerConfigService),
      TestBed.inject(ApolloClient),
    );

    fixture = TestBed.createComponent(ConsoleButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
