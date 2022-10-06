import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ConfirmButtonComponent } from './confirm-button.component';
import { ConfirmButtonModule } from './confirm-button.module';

describe('ConfirmButtonComponent', () => {
  let component: ConfirmButtonComponent;
  let fixture: ComponentFixture<ConfirmButtonComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [ConfirmButtonModule],
      }).compileComponents();

      fixture = TestBed.createComponent(ConfirmButtonComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    })
  );

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
