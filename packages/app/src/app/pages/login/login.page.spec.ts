import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { LoginPage } from './login.page';
import { LoginPageModule } from './login.module';
import { RouterTestingModule } from '@angular/router/testing';

describe('LoginPage', () => {
  let component: LoginPage;
  let fixture: ComponentFixture<LoginPage>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [LoginPageModule, RouterTestingModule],
      }).compileComponents();

      fixture = TestBed.createComponent(LoginPage);
      component = fixture.componentInstance;
      fixture.detectChanges();
    })
  );

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
