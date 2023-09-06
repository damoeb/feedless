import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { WizardColumnsComponent } from './wizard-columns.component';
import { WizardModule } from '../wizard.module';
import { AppTestModule } from '../../../app-test.module';

describe('WizardColumnsComponent', () => {
  let component: WizardColumnsComponent;
  let fixture: ComponentFixture<WizardColumnsComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [WizardModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(WizardColumnsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
