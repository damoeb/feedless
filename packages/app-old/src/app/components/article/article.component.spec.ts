import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ArticleComponent } from './article.component';
import { ArticleModule } from './article.module';
import { RouterTestingModule } from '@angular/router/testing';

describe('ArticleComponent', () => {
  let component: ArticleComponent;
  let fixture: ComponentFixture<ArticleComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [ArticleModule, RouterTestingModule],
      }).compileComponents();

      fixture = TestBed.createComponent(ArticleComponent);
      component = fixture.componentInstance;
      component.article = {} as any;
      component.articleRef = {} as any;
      fixture.detectChanges();
    })
  );

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
