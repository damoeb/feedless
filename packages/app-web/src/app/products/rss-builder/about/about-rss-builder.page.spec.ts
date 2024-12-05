import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AboutRssBuilderPage } from './about-rss-builder.page';
import {
  ApolloMockController,
  AppTestModule,
  mockServerSettings,
} from '../../../app-test.module';
import { ServerConfigService } from '../../../services/server-config.service';
import { ApolloClient } from '@apollo/client/core';
import { AppConfigService } from '../../../services/app-config.service';

describe('AboutRssBuilderPage', () => {
  let component: AboutRssBuilderPage;
  let fixture: ComponentFixture<AboutRssBuilderPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AboutRssBuilderPage, AppTestModule.withDefaults()],
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

    fixture = TestBed.createComponent(AboutRssBuilderPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
