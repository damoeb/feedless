import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { NotebookDetailsPage } from './notebook-details.page';
import { AppTestModule, mockRepositories } from '../../app-test.module';
import { NotebookService } from '../../services/notebook.service';
import { AuthGuardService } from '../../guards/auth-guard.service';

describe('NotebookDetailsPage', () => {
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
        {
          provide: NotebookService,
          useValue: {
            systemBusyChanges: { subscribe: jest.fn() },
            openNoteChanges: { subscribe: jest.fn() },
            notesChanges: {
              asObservable: jest.fn().mockReturnValue({ subscribe: jest.fn() }),
            },
            suggestByType: jest.fn().mockResolvedValue([]),
            openNotebook: jest.fn(),
            findById: jest.fn().mockResolvedValue({}),
            hasSettingsValue: jest.fn().mockReturnValue({
              pipe: jest.fn().mockReturnValue({ subscribe: jest.fn() }),
            }),
            getSettingsValue: jest.fn().mockReturnValue({
              pipe: jest.fn().mockReturnValue({ subscribe: jest.fn() }),
            }),
            createNote: jest.fn().mockResolvedValue({}),
            updateNote: jest.fn(),
            countChildren: jest.fn().mockReturnValue(0),
            findAllChildren: jest.fn().mockReturnValue([]),
            closeNoteChanges: { next: jest.fn() },
            deleteById: jest.fn(),
            showToast: jest.fn(),
            moveStartChanges: { next: jest.fn() },
            getSettingsOrDefault: jest.fn().mockReturnValue({
              pipe: jest.fn().mockReturnValue({ subscribe: jest.fn() }),
            }),
            openNoteById: jest.fn(),
          },
        },
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
