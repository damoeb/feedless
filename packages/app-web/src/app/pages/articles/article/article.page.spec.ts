import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ArticlePage } from './article.page';
import { ArticlePageModule } from './article.module';
import { AppTestModule } from '../../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';

describe('ArticlePage', () => {
  let component: ArticlePage;
  let fixture: ComponentFixture<ArticlePage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        ArticlePageModule,
        AppTestModule.withDefaults(),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ArticlePage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
