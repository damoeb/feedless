import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { WorkflowBuilderPage } from './workflow-builder.page';
import { AppTestModule } from '../../app-test.module';
import { WorkflowBuilderPageModule } from './workflow-builder.module';

describe('WorkflowBuilderPage', () => {
  let component: WorkflowBuilderPage;
  let fixture: ComponentFixture<WorkflowBuilderPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [WorkflowBuilderPageModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(WorkflowBuilderPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
