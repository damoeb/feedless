import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { NotebookBuilderPage } from './notebook-builder.page';
import { AppTestModule } from '../../app-test.module';
import { NotebookBuilderPageModule } from './notebook-builder.module';

describe('WorkflowBuilderPage', () => {
  let component: NotebookBuilderPage;
  let fixture: ComponentFixture<NotebookBuilderPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [NotebookBuilderPageModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(NotebookBuilderPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
