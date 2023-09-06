import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ArticleRefComponent } from './article-ref.component';
import { ArticleRefModule } from './article-ref.module';
import { AppTestModule } from '../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';
import { Article } from '../../graphql/types';

describe('ArticleRefComponent', () => {
  let component: ArticleRefComponent;
  let fixture: ComponentFixture<ArticleRefComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        ArticleRefModule,
        AppTestModule.withDefaults(),
        RouterTestingModule,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ArticleRefComponent);
    component = fixture.componentInstance;
    component.article = {
      webDocument: {
        pendingPlugins: [],
        enclosures: [],
      },
    } as Article;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
