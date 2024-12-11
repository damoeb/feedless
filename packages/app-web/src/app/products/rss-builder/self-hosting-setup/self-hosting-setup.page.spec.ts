import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SelfHostingSetupPage } from './self-hosting-setup.page';
import {
  ApolloMockController,
  AppTestModule,
  mockServerSettings,
} from '../../../app-test.module';
import { ServerConfigService } from '../../../services/server-config.service';
import { ApolloClient } from '@apollo/client/core';
import { AppConfigService } from '../../../services/app-config.service';

describe('SetupPage', () => {
  let component: SelfHostingSetupPage;
  let fixture: ComponentFixture<SelfHostingSetupPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SelfHostingSetupPage, AppTestModule.withDefaults()],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerConfigService),
      TestBed.inject(ApolloClient),
    );

    const appConfigService = TestBed.inject(AppConfigService);
    appConfigService.getAllAppConfigs = () =>
      Promise.resolve([
        {
          id: 'rss-proxy',
        } as any,
      ]);

    fixture = TestBed.createComponent(SelfHostingSetupPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
