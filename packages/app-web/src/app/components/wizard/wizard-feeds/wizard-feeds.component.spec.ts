import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { WizardFeedsComponent } from './wizard-feeds.component';
import { WizardModule } from '../wizard.module';
import { AppTestModule } from '../../../app-test.module';
import { FeedService } from '../../../services/feed.service';
import { ServerSettingsService } from '../../../services/server-settings.service';
import { WizardHandler } from '../wizard-handler';
import { defaultWizardContext } from '../wizard/wizard.component';

describe('WizardFeedsComponent', () => {
  let component: WizardFeedsComponent;
  let fixture: ComponentFixture<WizardFeedsComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [WizardModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(WizardFeedsComponent);
    component = fixture.componentInstance;
    const feedService = TestBed.inject(FeedService);
    const serverSettingsService = TestBed.inject(ServerSettingsService);

    component.handler = new WizardHandler(
      defaultWizardContext,
      feedService,
      serverSettingsService
    );
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
