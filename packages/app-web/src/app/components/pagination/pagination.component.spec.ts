import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { PaginationComponent } from './pagination.component';
import { PaginationModule } from './pagination.module';
import { ApolloMockController, AppTestModule, mockServerSettings } from '../../app-test.module';
import { Agents, GqlAgentsQuery, GqlAgentsQueryVariables } from '../../../generated/graphql';
import { ServerSettingsService } from '../../services/server-settings.service';
import { ApolloClient } from '@apollo/client/core';

describe('AgentsComponent', () => {
  let component: PaginationComponent;
  let fixture: ComponentFixture<PaginationComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        PaginationModule,
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
      TestBed.inject(ServerSettingsService),
      TestBed.inject(ApolloClient),
    );

    fixture = TestBed.createComponent(PaginationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
