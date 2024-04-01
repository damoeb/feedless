import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RepositoryPluginsPage } from './repository-plugins.page';
import { RepositoryPluginsPageModule } from './repository-plugins.module';
import { RouterTestingModule } from '@angular/router/testing';
import {
  AppTestModule,
  mockSourceSubscription,
} from '../../../../app-test.module';
import {
  GqlListPluginsQuery,
  GqlListPluginsQueryVariables,
  ListPlugins,
} from '../../../../../generated/graphql';

describe('RepositoryPluginsPage', () => {
  let component: RepositoryPluginsPage;
  let fixture: ComponentFixture<RepositoryPluginsPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        RepositoryPluginsPageModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockSourceSubscription(apolloMockController);
          apolloMockController
            .mockQuery<GqlListPluginsQuery, GqlListPluginsQueryVariables>(
              ListPlugins,
            )
            .and.resolveOnce(async () => {
              return {
                data: {
                  plugins: [],
                },
              };
            });
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RepositoryPluginsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
