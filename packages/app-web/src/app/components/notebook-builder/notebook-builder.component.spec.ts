import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { NotebookBuilderComponent } from './notebook-builder.component';
import { NotebookBuilderModule } from './notebook-builder.module';
import { AppTestModule, mockRepositories } from '../../app-test.module';

describe('NotebookBuilderComponent', () => {
  let component: NotebookBuilderComponent;
  let fixture: ComponentFixture<NotebookBuilderComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        NotebookBuilderModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockRepositories(apolloMockController);
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(NotebookBuilderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
