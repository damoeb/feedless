import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { AgentsPage } from './agents.page';
import { AgentsPageModule } from './agents.module';
import { AppTestModule } from '../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';
import {
  GqlSearchBucketsOrFeedsQuery,
  GqlSearchBucketsOrFeedsQueryVariables,
  SearchBucketsOrFeeds,
} from '../../../generated/graphql';

describe('BucketsPage', () => {
  let component: AgentsPage;
  let fixture: ComponentFixture<AgentsPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        AgentsPageModule,
        AppTestModule.withDefaults((apolloMockController) => {
          apolloMockController
            .mockQuery<
              GqlSearchBucketsOrFeedsQuery,
              GqlSearchBucketsOrFeedsQueryVariables
            >(SearchBucketsOrFeeds)
            .and.resolveOnce(async () => {
              return {
                data: {
                  bucketsOrNativeFeeds: [],
                },
              };
            });
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AgentsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
