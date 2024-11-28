import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { NotebooksComponent } from './notebooks.component';
import { AppTestModule, mockRepositories } from '../../app-test.module';
import { AuthGuardService } from '../../guards/auth-guard.service';

describe('NotebooksComponent', () => {
  let component: NotebooksComponent;
  let fixture: ComponentFixture<NotebooksComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        NotebooksComponent,
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

    fixture = TestBed.createComponent(NotebooksComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
