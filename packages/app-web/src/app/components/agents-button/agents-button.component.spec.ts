import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { AgentsButtonComponent } from './agents-button.component';
import { AgentsButtonModule } from './agents-button.module';
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

describe('AgentsButtonComponent', () => {
  let component: AgentsButtonComponent;
  let fixture: ComponentFixture<AgentsButtonComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        AgentsButtonModule,
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
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
