import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { LoginButtonComponent } from './login-button.component';
import { LoginButtonModule } from './login-button.module';

describe('BubbleComponent', () => {
  let component: LoginButtonComponent;
  let fixture: ComponentFixture<LoginButtonComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [LoginButtonModule]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
