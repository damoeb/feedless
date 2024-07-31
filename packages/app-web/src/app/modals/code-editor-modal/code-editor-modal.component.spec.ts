import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { CodeEditorModalComponent } from './code-editor-modal.component';
import { CodeEditorModalModule } from './code-editor-modal.module';
import { AppTestModule } from '../../app-test.module';

describe('CodeEditorModalComponent', () => {
  let component: CodeEditorModalComponent;
  let fixture: ComponentFixture<CodeEditorModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [CodeEditorModalModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(CodeEditorModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
