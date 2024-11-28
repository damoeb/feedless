import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { DirectoryPage } from './directory.page';
import { AppTestModule } from '../../app-test.module';
import {
  GqlListPublicRepositoriesQuery,
  GqlListPublicRepositoriesQueryVariables,
  ListPublicRepositories,
} from '../../../generated/graphql';

describe('DirectoryPage', () => {
  let component: DirectoryPage;
  let fixture: ComponentFixture<DirectoryPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        DirectoryPage,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) =>
            apolloMockController
              .mockQuery<
                GqlListPublicRepositoriesQuery,
                GqlListPublicRepositoriesQueryVariables
              >(ListPublicRepositories)
              .and.resolveOnce(async () => {
                return {
                  data: {
                    repositories: [],
                  },
                };
              }),
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DirectoryPage);
    component = fixture.componentInstance;
    component.repositories = [];
    component.documents = [];
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
