import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { AgentsComponent } from './agents.component';
import { AgentsModule } from './agents.module';
import { AppTestModule } from '../../app-test.module';
import {
  Agents,
  GqlAgentsQuery,
  GqlAgentsQueryVariables,
} from '../../../generated/graphql';

describe('AgentsComponent', () => {
  let component: AgentsComponent;
  let fixture: ComponentFixture<AgentsComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
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

    fixture = TestBed.createComponent(AgentsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
