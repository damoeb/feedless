import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AgentsComponent } from './agents.component';
import {
  ApolloMockController,
  AppTestModule,
  mockServerSettings,
} from '@feedless/testing';
import {
  Agents,
  GqlAgentsQuery,
  GqlAgentsQueryVariables,
} from '@feedless/graphql-api';
import { ServerConfigService } from '@feedless/services';
import { ApolloClient } from '@apollo/client/core';

describe('AgentsComponent', () => {
  let component: AgentsComponent;
  let fixture: ComponentFixture<AgentsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        AgentsComponent,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) =>
            apolloMockController
              .mockQuery<GqlAgentsQuery, GqlAgentsQueryVariables>(Agents)
              .and.resolveOnce(async () => {
                return {
                  data: {
                    agents: [],
                  },
                };
              }),
        }),
      ],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerConfigService),
      TestBed.inject(ApolloClient),
    );

    fixture = TestBed.createComponent(AgentsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
