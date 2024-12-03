import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { SetupPage } from './setup.page';
import {
  ApolloMockController,
  AppTestModule,
  mockServerSettings,
} from '../../../app-test.module';
import { ServerConfigService } from '../../../services/server-config.service';
import { ApolloClient } from '@apollo/client/core';
import { AppConfigService } from '../../../services/app-config.service';

describe('SetupPage', () => {
  let component: SetupPage;
  let fixture: ComponentFixture<SetupPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SetupPage, AppTestModule.withDefaults()],
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

    fixture = TestBed.createComponent(SetupPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
