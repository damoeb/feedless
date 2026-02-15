import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AboutUntoldNotesPage } from './about-untold-notes.page';
import { AppTestModule, mockRepositories } from '../../../app-test.module';

xdescribe('AboutUntoldNotesPage', () => {
  let component: AboutUntoldNotesPage;
  let fixture: ComponentFixture<AboutUntoldNotesPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        AboutUntoldNotesPage,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) => mockRepositories(apolloMockController),
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AboutUntoldNotesPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
