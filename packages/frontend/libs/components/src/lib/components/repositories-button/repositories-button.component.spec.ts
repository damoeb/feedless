import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RepositoriesButtonComponent } from './repositories-button.component';
import {
  ApolloMockController,
  AppTestModule,
  mockServerSettings,
} from '@feedless/test';
import {
  Agents,
  GqlAgentsQuery,
  GqlAgentsQueryVariables,
} from '@feedless/graphql-api';
import { ServerConfigService } from '@feedless/services';
import { ApolloClient } from '@apollo/client/core';

describe('RepositoriesButtonComponent', () => {
  let component: RepositoriesButtonComponent;
  let fixture: ComponentFixture<RepositoriesButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        RepositoriesButtonComponent,
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

    fixture = TestBed.createComponent(RepositoriesButtonComponent);
    component = fixture.componentInstance;
    const componentRef = fixture.componentRef;
    componentRef.setInput('name', '');
    componentRef.setInput('link', '');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
