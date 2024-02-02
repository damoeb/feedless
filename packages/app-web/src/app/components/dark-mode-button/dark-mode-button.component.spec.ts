import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { DarkModeButtonComponent } from './dark-mode-button.component';
import { DarkModeButtonModule } from './dark-mode-button.module';
import { AppTestModule } from '../../app-test.module';

describe('DarkModeButtonComponent', () => {
  let component: DarkModeButtonComponent;
  let fixture: ComponentFixture<DarkModeButtonComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [DarkModeButtonModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(DarkModeButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
