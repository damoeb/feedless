import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { IonicModule } from '@ionic/angular';

import { WizardGenericFeedsComponent } from './wizard-generic-feeds.component';

describe('WizardGenericFeedsComponent', () => {
  let component: WizardGenericFeedsComponent;
  let fixture: ComponentFixture<WizardGenericFeedsComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [WizardGenericFeedsComponent],
      imports: [IonicModule.forRoot()],
    }).compileComponents();

    fixture = TestBed.createComponent(WizardGenericFeedsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
