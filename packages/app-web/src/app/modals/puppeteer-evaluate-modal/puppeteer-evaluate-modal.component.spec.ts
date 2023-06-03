import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { IonicModule } from '@ionic/angular';

import { PuppeteerEvaluateModalComponent } from './puppeteer-evaluate-modal.component';

describe('DiscoveryModalComponent', () => {
  let component: PuppeteerEvaluateModalComponent;
  let fixture: ComponentFixture<PuppeteerEvaluateModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [PuppeteerEvaluateModalComponent],
      imports: [IonicModule.forRoot()],
    }).compileComponents();

    fixture = TestBed.createComponent(PuppeteerEvaluateModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
