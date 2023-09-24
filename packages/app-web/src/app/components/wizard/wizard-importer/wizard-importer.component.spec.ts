import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { WizardImporterComponent } from './wizard-importer.component';
import { WizardModule } from '../wizard.module';
import { AppTestModule } from '../../../app-test.module';
import { FeedService } from '../../../services/feed.service';
import { ServerSettingsService } from '../../../services/server-settings.service';
import { WizardHandler } from '../wizard-handler';
import {
  defaultWizardContext,
  WizardContext,
} from '../wizard/wizard.component';

describe('WizardImporterComponent', () => {
  let component: WizardImporterComponent;
  let fixture: ComponentFixture<WizardImporterComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [WizardModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(WizardImporterComponent);
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
