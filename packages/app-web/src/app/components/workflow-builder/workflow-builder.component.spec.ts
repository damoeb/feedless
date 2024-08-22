import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { WorkflowBuilderComponent } from './workflow-builder.component';
import { WorkflowBuilderModule } from './workflow-builder.module';
import { AppTestModule, mockRepositories } from '../../app-test.module';

describe('WorkflowBuilderComponent', () => {
  let component: WorkflowBuilderComponent;
  let fixture: ComponentFixture<WorkflowBuilderComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        WorkflowBuilderModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockRepositories(apolloMockController);
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(WorkflowBuilderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
