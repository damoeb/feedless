import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { CodeEditorModalComponent } from './code-editor-modal.component';
import { AppTestModule } from '@feedless/testing';

describe('CodeEditorModalComponent', () => {
  let component: CodeEditorModalComponent;
  let fixture: ComponentFixture<CodeEditorModalComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [CodeEditorModalComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(CodeEditorModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
