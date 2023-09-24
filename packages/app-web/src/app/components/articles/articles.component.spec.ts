import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ArticlesComponent } from './articles.component';
import { ArticlesModule } from './articles.module';
import { AppTestModule } from '../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';
import {
  GqlSearchArticlesQuery,
  GqlSearchArticlesQueryVariables,
  SearchArticles,
} from '../../../generated/graphql';

describe('ArticlesComponent', () => {
  let component: ArticlesComponent;
  let fixture: ComponentFixture<ArticlesComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        ArticlesModule,
        AppTestModule.withDefaults((apolloMockController) => {
          apolloMockController
            .mockQuery<GqlSearchArticlesQuery, GqlSearchArticlesQueryVariables>(
              SearchArticles,
            )
            .and.resolveOnce(async () => {
              return {
                data: {
                  articles: {
                    articles: [],
                    pagination: {} as any,
                  },
                },
              };
            });
        }),
        RouterTestingModule,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ArticlesComponent);
    component = fixture.componentInstance;
    const clock = jasmine.clock().install();
    fixture.detectChanges();
    clock.tick(15000);
    clock.uninstall();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
