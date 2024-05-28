import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { AgentsComponent } from './agents.component';
import { AgentsModule } from './agents.module';
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

describe('AgentsComponent', () => {
  let component: AgentsComponent;
  let fixture: ComponentFixture<AgentsComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        AgentsModule,
        AppTestModule.withDefaults((apolloMockController) => {
          apolloMockController
            .mockQuery<GqlAgentsQuery, GqlAgentsQueryVariables>(Agents)
            .and.resolveOnce(async () => {
              return {
                data: {
                  agents: [],
                },
              };
            });
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
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
