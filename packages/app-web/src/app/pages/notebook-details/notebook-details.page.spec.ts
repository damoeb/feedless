import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { NotebookDetailsPage } from './notebook-details.page';
import { AppTestModule, mockRepositories } from '../../app-test.module';

fdescribe('NotebookDetailsPage', () => {
  let component: NotebookDetailsPage;
  let fixture: ComponentFixture<NotebookDetailsPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        NotebookDetailsPage,
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
