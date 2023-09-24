import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { WizardNativeFeedComponent } from './wizard-native-feed.component';
import { WizardModule } from '../wizard.module';
import { AppTestModule } from '../../../app-test.module';
import { WizardHandler } from '../wizard-handler';
import { FeedService } from '../../../services/feed.service';
import { ServerSettingsService } from '../../../services/server-settings.service';
import {
  defaultWizardContext,
  WizardContext,
} from '../wizard/wizard.component';

describe('WizardNativeFeedComponent', () => {
  let component: WizardNativeFeedComponent;
  let fixture: ComponentFixture<WizardNativeFeedComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [WizardModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(WizardNativeFeedComponent);
    component = fixture.componentInstance;

    const feedService = TestBed.inject(FeedService);
    const serverSettingsService = TestBed.inject(ServerSettingsService);
    const context: WizardContext = {
      ...defaultWizardContext,
      feed: {},
    };
    component.handler = new WizardHandler(
      context,
      feedService,
      serverSettingsService,
    );
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
