import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { EmailLoginComponent } from './email-login.component';
import { AppTestModule } from '../../app-test.module';
import { EmailLoginModule } from './email-login.module';

describe('EmailLoginComponent', () => {
  let component: EmailLoginComponent;
  let fixture: ComponentFixture<EmailLoginComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [EmailLoginModule, AppTestModule.withDefaults()]
    }).compileComponents();

    fixture = TestBed.createComponent(EmailLoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
