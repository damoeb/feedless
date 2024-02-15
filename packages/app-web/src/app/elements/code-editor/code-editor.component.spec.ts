import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { CodeEditorComponent } from './code-editor.component';
import { CodeEditorModule } from './code-editor.module';

describe('CodeEditorComponent', () => {
  let component: CodeEditorComponent;
  let fixture: ComponentFixture<CodeEditorComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [CodeEditorModule],
    }).compileComponents();

    fixture = TestBed.createComponent(CodeEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
