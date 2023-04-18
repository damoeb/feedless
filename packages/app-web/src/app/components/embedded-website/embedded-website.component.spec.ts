import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { IonicModule } from '@ionic/angular';

import { EmbeddedWebsiteComponent } from './embedded-website.component';

describe('FeedDiscoveryWizardComponent', () => {
  let component: EmbeddedWebsiteComponent;
  let fixture: ComponentFixture<EmbeddedWebsiteComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [EmbeddedWebsiteComponent],
      imports: [IonicModule.forRoot()],
    }).compileComponents();

    fixture = TestBed.createComponent(EmbeddedWebsiteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
