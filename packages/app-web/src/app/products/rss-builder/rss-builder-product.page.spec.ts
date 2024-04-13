import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RssBuilderProductPage } from './rss-builder-product.page';
import {
  ApolloMockController,
  AppTestModule,
  mockLicense,
  mockScrape,
  mockServerSettings,
} from '../../app-test.module';
import { RssBuilderProductModule } from './rss-builder-product.module';
import { RouterTestingModule } from '@angular/router/testing';
import { ServerSettingsService } from '../../services/server-settings.service';
import { ApolloClient } from '@apollo/client/core';

describe('RssBuilderProductPage', () => {
  let component: RssBuilderProductPage;
  let fixture: ComponentFixture<RssBuilderProductPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        RssBuilderProductModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockScrape(apolloMockController);
          mockLicense(apolloMockController);
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerSettingsService),
      TestBed.inject(ApolloClient),
    );

    fixture = TestBed.createComponent(RssBuilderProductPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
