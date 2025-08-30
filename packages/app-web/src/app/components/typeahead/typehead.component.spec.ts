import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TypeheadComponent } from './typehead.component';
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

describe('TypeaheadComponent', () => {
  let component: TypeheadComponent;
  let fixture: ComponentFixture<TypeheadComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        TypeheadComponent,
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

    fixture = TestBed.createComponent(TypeheadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
