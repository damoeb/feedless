import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { IonicModule } from '@ionic/angular';

import { WizardNativeFeedComponent } from './wizard-native-feed.component';

describe('WizardNativeFeedComponent', () => {
  let component: WizardNativeFeedComponent;
  let fixture: ComponentFixture<WizardNativeFeedComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [WizardNativeFeedComponent],
      imports: [IonicModule.forRoot()],
    }).compileComponents();

    fixture = TestBed.createComponent(WizardNativeFeedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
