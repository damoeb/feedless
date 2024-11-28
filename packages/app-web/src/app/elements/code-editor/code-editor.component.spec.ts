import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { CodeEditorComponent } from './code-editor.component';
import { AppTestModule } from '../../app-test.module';

describe('CodeEditorComponent', () => {
  let component: CodeEditorComponent;
  let fixture: ComponentFixture<CodeEditorComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [AppTestModule.withDefaults(), CodeEditorComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(CodeEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
