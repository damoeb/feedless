import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RepositoriesButtonComponent } from './repositories-button.component';
import { RepositoriesButtonModule } from './repositories-button.module';
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

describe('RepositoriesButtonComponent', () => {
  let component: RepositoriesButtonComponent;
  let fixture: ComponentFixture<RepositoriesButtonComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        RepositoriesButtonModule,
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

    fixture = TestBed.createComponent(RepositoriesButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
