import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RepositoryDetailsPage } from './repository-details.page';
import { RepositoryDetailsPageModule } from './repository-details.module';
import { RouterTestingModule } from '@angular/router/testing';
import { AppTestModule } from '../../../app-test.module';
import {
  GqlSourceSubscription,
  GqlSourceSubscriptionByIdQuery,
  GqlSourceSubscriptionByIdQueryVariables,
  GqlVisibility,
  SourceSubscriptionById,
} from '../../../../generated/graphql';

describe('RepositoryDetailsPage', () => {
  let component: RepositoryDetailsPage;
  let fixture: ComponentFixture<RepositoryDetailsPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        RepositoryDetailsPageModule,
        AppTestModule.withDefaults((apolloMockController) => {
          apolloMockController
            .mockQuery<
              GqlSourceSubscriptionByIdQuery,
              GqlSourceSubscriptionByIdQueryVariables
            >(SourceSubscriptionById)
            .and.resolveOnce(async () => {
              const sourceSubscription: GqlSourceSubscription = {
                id: '',
                description: '',
                title: '',
                ownerId: '',
                sources: [],
                visibility: GqlVisibility.IsPrivate,
                createdAt: 0,
                retention: {},
              };
              return {
                data: {
                  sourceSubscription: sourceSubscription,
                },
              };
            });
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RepositoryDetailsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
