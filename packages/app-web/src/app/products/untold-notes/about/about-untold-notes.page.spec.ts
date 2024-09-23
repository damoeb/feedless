import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { AboutUntoldNotesPage } from './about-untold-notes.page';
import { AppTestModule, mockRepositories } from '../../../app-test.module';
import { AboutUntoldNotesModule } from './about-untold-notes.module';
import { UntoldNotesProductModule } from '../untold-notes-product.module';

describe('AboutUntoldNotesPage', () => {
  let component: AboutUntoldNotesPage;
  let fixture: ComponentFixture<AboutUntoldNotesPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        AboutUntoldNotesModule,
        UntoldNotesProductModule,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) =>
            mockRepositories(apolloMockController),
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AboutUntoldNotesPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
