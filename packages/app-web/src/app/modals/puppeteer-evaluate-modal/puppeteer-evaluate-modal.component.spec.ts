import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { PuppeteerEvaluateModalComponent } from './puppeteer-evaluate-modal.component';
import { PuppeteerEvaluateModalModule } from './puppeteer-evaluate-modal.module';

describe('PuppeteerEvaluateModalComponent', () => {
  let component: PuppeteerEvaluateModalComponent;
  let fixture: ComponentFixture<PuppeteerEvaluateModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [PuppeteerEvaluateModalModule],
    }).compileComponents();

    fixture = TestBed.createComponent(PuppeteerEvaluateModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
