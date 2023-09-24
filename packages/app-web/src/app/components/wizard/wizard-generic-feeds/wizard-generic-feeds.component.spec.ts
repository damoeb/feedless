import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { WizardGenericFeedsComponent } from './wizard-generic-feeds.component';
import { WizardModule } from '../wizard.module';
import {
  ApolloMockController,
  AppTestModule,
  mockScrape,
  mockServerSettings,
} from '../../../app-test.module';
import { FeedService } from '../../../services/feed.service';
import { ServerSettingsService } from '../../../services/server-settings.service';
import { WizardHandler } from '../wizard-handler';
import {
  defaultWizardContext,
  WizardContext,
} from '../wizard/wizard.component';
import { GqlPuppeteerWaitUntil } from '../../../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';

xdescribe('WizardGenericFeedsComponent', () => {
  let component: WizardGenericFeedsComponent;
  let fixture: ComponentFixture<WizardGenericFeedsComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        WizardModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockScrape(apolloMockController);
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(WizardGenericFeedsComponent);
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
      feed: {
        create: {
          genericFeed: {
            specification: {
              selectors: {
                contextXPath: '',
                dateXPath: '',
                linkXPath: '',
              },
            },
          } as any,
        },
      },
    };
    const wizardHandler = new WizardHandler(
      context,
      feedService,
      serverSettingsService,
    );
    await wizardHandler.init(false);
    component.handler = wizardHandler;
    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerSettingsService),
      TestBed.inject(ApolloClient),
    );
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
