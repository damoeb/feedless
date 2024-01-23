import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { MailCodeLoginComponent } from './mail-code-login.component';
import { AppTestModule } from '../../app-test.module';
import { MailCodeLoginModule } from './mail-code-login.module';

describe('MailCodeLoginComponent', () => {
  let component: MailCodeLoginComponent;
  let fixture: ComponentFixture<MailCodeLoginComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [MailCodeLoginModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(MailCodeLoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
