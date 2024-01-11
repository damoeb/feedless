import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ResponsiveColumnsComponent } from './responsive-columns.component';
import { WizardModule } from '../wizard.module';
import { AppTestModule } from '../../app-test.module';

describe('WizardColumnsComponent', () => {
  let component: ResponsiveColumnsComponent;
  let fixture: ComponentFixture<ResponsiveColumnsComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [WizardModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(ResponsiveColumnsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
