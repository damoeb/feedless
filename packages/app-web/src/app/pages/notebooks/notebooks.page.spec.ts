import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { NotebooksPage } from './notebooks.page';
import { AppTestModule, mockRepositories } from '../../app-test.module';
import { AuthGuardService } from '../../guards/auth-guard.service';

describe('NotebooksPage', () => {
  let component: NotebooksPage;
  let fixture: ComponentFixture<NotebooksPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        NotebooksPage,
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
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(NotebooksPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
