import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RepositoryDataPage } from './repository-data.page';
import { RepositoryDataPageModule } from './repository-data.module';
import { RouterTestingModule } from '@angular/router/testing';
import { AppTestModule } from '../../../../app-test.module';
import {
  GqlWebDocumentByIdsQuery,
  GqlWebDocumentByIdsQueryVariables,
  WebDocumentByIds,
} from '../../../../../generated/graphql';

describe('RepositoryDataPage', () => {
  let component: RepositoryDataPage;
  let fixture: ComponentFixture<RepositoryDataPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        RepositoryDataPageModule,
        AppTestModule.withDefaults((apolloMockController) => {
          apolloMockController
            .mockQuery<
              GqlWebDocumentByIdsQuery,
              GqlWebDocumentByIdsQueryVariables
            >(WebDocumentByIds)
            .and.resolveOnce(async () => {
              return {
                data: {
                  webDocuments: [],
                },
              };
            });
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RepositoryDataPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
