import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { LanguageButtonComponent } from './language-button.component';
import { LanguageButtonModule } from './language-button.module';
import { AppTestModule } from '../../app-test.module';

describe('DarkModeButtonComponent', () => {
  let component: LanguageButtonComponent;
  let fixture: ComponentFixture<LanguageButtonComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [LanguageButtonModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(LanguageButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
