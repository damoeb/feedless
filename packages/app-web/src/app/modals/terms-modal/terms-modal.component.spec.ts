import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { TermsModalComponent } from './terms-modal.component';
import { TermsModalModule } from './terms-modal.module';
import { AppTestModule } from '../../app-test.module';

describe('TermsModalComponent', () => {
  let component: TermsModalComponent;
  let fixture: ComponentFixture<TermsModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [TermsModalModule, AppTestModule.withDefaults()]
    }).compileComponents();

    fixture = TestBed.createComponent(TermsModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
