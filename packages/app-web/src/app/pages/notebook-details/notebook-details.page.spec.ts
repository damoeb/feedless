import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { NotebookDetailsPage } from './notebook-details.page';
import { AppTestModule, mockRepositories } from '../../app-test.module';
import { NotebookDetailsPageModule } from './notebook-details.module';
import { UntoldNotesProductModule } from '../../products/untold-notes/untold-notes-product.module';

describe('NotebookDetailsDetailsPage', () => {
  let component: NotebookDetailsPage;
  let fixture: ComponentFixture<NotebookDetailsPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        NotebookDetailsPageModule,
        UntoldNotesProductModule,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) =>
            mockRepositories(apolloMockController),
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(NotebookDetailsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
