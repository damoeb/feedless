import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { AboutRssBuilderPage } from './about-rss-builder.page';
import {
  ApolloMockController,
  AppTestModule,
  mockLicense,
  mockServerSettings,
} from '../../../app-test.module';
import { AboutRssBuilderModule } from './about-rss-builder.module';
import { ServerSettingsService } from '../../../services/server-settings.service';
import { ApolloClient } from '@apollo/client/core';

describe('AboutRssBuilderPage', () => {
  let component: AboutRssBuilderPage;
  let fixture: ComponentFixture<AboutRssBuilderPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        AboutRssBuilderModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockLicense(apolloMockController);
        }),
      ],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerSettingsService),
      TestBed.inject(ApolloClient),
    );

    fixture = TestBed.createComponent(AboutRssBuilderPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
