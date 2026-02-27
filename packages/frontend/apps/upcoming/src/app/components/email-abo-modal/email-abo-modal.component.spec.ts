import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmailAboModalComponent } from './email-abo-modal.component';
import { SubmitModalModule } from './submit-modal.module';
import { AppTestModule } from '@feedless/testing';

describe('EmailAboModalComponent', () => {
  let component: EmailAboModalComponent;
  let fixture: ComponentFixture<EmailAboModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SubmitModalModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(EmailAboModalComponent);
    component = fixture.componentInstance;
    component.location = {} as any;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
