import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { TrialWarningComponent } from './trial-warning.component';
import { TrialWarningModule } from './trial-warning.module';
import { AppTestModule, mockLicense } from '../../app-test.module';

describe('TrialWarningComponent', () => {
  let component: TrialWarningComponent;
  let fixture: ComponentFixture<TrialWarningComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [TrialWarningModule, AppTestModule.withDefaults(apolloMockController => {
        mockLicense(apolloMockController);
      })],
    }).compileComponents();

    fixture = TestBed.createComponent(TrialWarningComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
