import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AgentsButtonComponent } from './agents-button.component';
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

describe('AgentsButtonComponent', () => {
  let component: AgentsButtonComponent;
  let fixture: ComponentFixture<AgentsButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        AgentsButtonComponent,
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

    fixture = TestBed.createComponent(AgentsButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
