import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { WizardFetchOptionsComponent } from './wizard-fetch-options.component';
import { WizardModule } from '../wizard.module';
import { AppTestModule } from '../../../app-test.module';
import { FeedService } from '../../../services/feed.service';
import { ServerSettingsService } from '../../../services/server-settings.service';
import { WizardHandler } from '../wizard-handler';
import { defaultWizardContext } from '../wizard/wizard.component';

describe('WizardFetchOptionsComponent', () => {
  let component: WizardFetchOptionsComponent;
  let fixture: ComponentFixture<WizardFetchOptionsComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [WizardModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(WizardFetchOptionsComponent);
    component = fixture.componentInstance;
    const feedService = TestBed.inject(FeedService);
    const serverSettingsService = TestBed.inject(ServerSettingsService);

    component.handler = new WizardHandler(
      defaultWizardContext,
      feedService,
      serverSettingsService,
    );
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
