import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { TextDiffComponent } from './text-diff.component';
import { AppTestModule } from '../../app-test.module';
import { TextDiffModule } from './text-diff.module';
import { WebDocument } from '../../graphql/types';

describe('TextDiffComponent', () => {
  let component: TextDiffComponent;
  let fixture: ComponentFixture<TextDiffComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [TextDiffModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(TextDiffComponent);
    component = fixture.componentInstance;
    component.before = {
      contentHtml: '',
      contentText: '',
      contentRawBase64: '',
    } as WebDocument;
    component.after = {
      contentHtml: '',
      contentText: '',
      contentRawBase64: '',
    } as WebDocument;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
