import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { LoginButtonComponent } from './login-button.component';
import { AppTestModule } from '../../app-test.module';

describe('LoginButtonComponent', () => {
  let component: LoginButtonComponent;
  let fixture: ComponentFixture<LoginButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoginButtonComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
