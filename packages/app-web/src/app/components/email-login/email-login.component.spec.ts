import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmailLoginComponent } from './email-login.component';
import { AppTestModule } from '../../app-test.module';

describe('EmailLoginComponent', () => {
  let component: EmailLoginComponent;
  let fixture: ComponentFixture<EmailLoginComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmailLoginComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(EmailLoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
