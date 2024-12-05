import { ComponentFixture, TestBed } from '@angular/core/testing';
import { WorkflowBuilderPage } from './workflow-builder.page';
import { AppTestModule } from '../../app-test.module';

describe('WorkflowBuilderPage', () => {
  let component: WorkflowBuilderPage;
  let fixture: ComponentFixture<WorkflowBuilderPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WorkflowBuilderPage, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(WorkflowBuilderPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
