import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CodeEditorComponent } from './code-editor.component';
import { AppTestModule } from '@feedless/testing';

describe.skip('CodeEditorComponent', () => {
  let component: CodeEditorComponent;
  let fixture: ComponentFixture<CodeEditorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CodeEditorComponent, AppTestModule],
    }).compileComponents();

    fixture = TestBed.createComponent(CodeEditorComponent);
    component = fixture.componentInstance;
  });

  function getRenderedContent(text: string) {
    fixture.componentRef.setInput('text', text);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    return compiled.querySelector('.cm-activeLine').innerHTML.trim();
  }

  it('renders checkbox', () => {
    expect(getRenderedContent('[ ]')).toEqual(
      `<span class="cm-checkbox">[ ]</span>`,
    );
  });

  it('renders blockquote', () => {
    expect(getRenderedContent('> blockquote')).toEqual(
      `<span class="cm-checkbox">[ ]</span>`,
    );
  });

  it('renders hashtag', () => {
    expect(getRenderedContent('#tag')).toEqual(
      `<span class="cm-hashtag">#tag</span>`,
    );
  });

  it('renders transclude note', () => {
    expect(getRenderedContent('actual note content ![other-note-id]')).toEqual(
      `<span class="cm-hashtag">#tag</span>`,
    );
  });

  it('renders embed image', () => {
    expect(getRenderedContent('!(Isolated.png "Title")')).toEqual(
      `<span class="cm-hashtag">#tag</span>`,
    );
  });

  it('renders annotation', () => {
    // expect(getRenderedContent('#tag')).toEqual(`<span class="cm-hashtag">#tag</span>`);
  });

  it('renders url', () => {
    expect(getRenderedContent('http://example.com')).toEqual(
      `<span class="cm-url">http://example.com</span>`,
    );
  });
});
