import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { IonicModule } from '@ionic/angular';

import { FeedDiscoveryWizardComponent } from './feed-discovery-wizard.component';

describe('FeedDiscoveryWizardComponent', () => {
  let component: FeedDiscoveryWizardComponent;
  let fixture: ComponentFixture<FeedDiscoveryWizardComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [FeedDiscoveryWizardComponent],
      imports: [IonicModule.forRoot()],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedDiscoveryWizardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
