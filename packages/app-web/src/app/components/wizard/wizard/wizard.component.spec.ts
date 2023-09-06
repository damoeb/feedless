import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { defaultWizardContext, WizardComponent } from './wizard.component';
import { WizardModule } from '../wizard.module';
import { AppTestModule } from '../../../app-test.module';
import { FeedService } from '../../../services/feed.service';
import { ServerSettingsService } from '../../../services/server-settings.service';
import { WizardHandler } from '../wizard-handler';

describe('WizardComponent', () => {
  let component: WizardComponent;
  let fixture: ComponentFixture<WizardComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [WizardModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(WizardComponent);
    component = fixture.componentInstance;
    const feedService = TestBed.inject(FeedService);
    const serverSettingsService = TestBed.inject(ServerSettingsService);
    const wizardHandler = new WizardHandler(
      defaultWizardContext,
      feedService,
      serverSettingsService
    );
    await wizardHandler.init(false);
    component.handler = wizardHandler;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
