import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { WorkflowBuilderComponent } from './workflow-builder.component';
import { AppTestModule, mockRepositories } from '../../app-test.module';

describe('WorkflowBuilderComponent', () => {
  let component: WorkflowBuilderComponent;
  let fixture: ComponentFixture<WorkflowBuilderComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        WorkflowBuilderComponent,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) => mockRepositories(apolloMockController),
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
