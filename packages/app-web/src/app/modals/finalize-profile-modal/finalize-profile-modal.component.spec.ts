import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { FinalizeProfileModalComponent } from './finalize-profile-modal.component';
import { FinalizeProfileModalModule } from './finalize-profile-modal.module';
import { AppTestModule } from '../../app-test.module';

describe('TermsModalComponent', () => {
  let component: FinalizeProfileModalComponent;
  let fixture: ComponentFixture<FinalizeProfileModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [FinalizeProfileModalModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(FinalizeProfileModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
