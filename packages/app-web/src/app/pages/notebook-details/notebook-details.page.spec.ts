import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { NotebookDetailsPage } from './notebook-details.page';
import { AppTestModule, mockRepositories } from '../../app-test.module';
import { NotebookService } from '../../services/notebook.service';
import { AuthGuardService } from '../../guards/auth-guard.service';

xdescribe('NotebookDetailsPage', () => {
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
      providers: [
        {
          provide: AuthGuardService,
          useValue: {
            assertLoggedIn: () => {},
          },
        },
        { provide: NotebookService, useValue: jest.fn().mockReturnValue({}) },
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
