import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { WizardPageChangeComponent } from './wizard-page-change.component';
import { WizardModule } from '../wizard.module';
import { AppTestModule, mockScrape } from '../../../app-test.module';
import { FeedService } from '../../../services/feed.service';
import { ServerSettingsService } from '../../../services/server-settings.service';
import { WizardHandler } from '../wizard-handler';
import {
  defaultWizardContext,
  WizardContext,
} from '../wizard/wizard.component';
import { GqlPuppeteerWaitUntil } from '../../../../generated/graphql';

describe('WizardPageChangeComponent', () => {
  let component: WizardPageChangeComponent;
  let fixture: ComponentFixture<WizardPageChangeComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        WizardModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockScrape(apolloMockController);
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(WizardPageChangeComponent);
    component = fixture.componentInstance;
    const feedService = TestBed.inject(FeedService);
    const serverSettingsService = TestBed.inject(ServerSettingsService);
    const context: WizardContext = {
      ...defaultWizardContext,
      fetchOptions: {
        prerender: false,
        prerenderWaitUntil: GqlPuppeteerWaitUntil.Load,
        prerenderScript: '',
        websiteUrl: 'https://example.org',
      },
    };
    const wizardHandler = new WizardHandler(
      context,
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
