import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { NotebookSettingsComponent } from './notebook-settings.component';
import { AppTestModule, mockRepositories } from '../../app-test.module';
import { AuthGuardService } from '../../guards/auth-guard.service';
import { NotebookService } from '../../services/notebook.service';

describe('NotebookSettingsComponent', () => {
  let component: NotebookSettingsComponent;
  let fixture: ComponentFixture<NotebookSettingsComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        NotebookSettingsComponent,
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

    fixture = TestBed.createComponent(NotebookSettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
