import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RssBuilderProductPage } from './rss-builder-product.page';
import {
  ApolloMockController,
  AppTestModule,
  mockScrape,
  mockServerSettings,
} from '../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';
import { ServerConfigService } from '../../services/server-config.service';
import { ApolloClient } from '@apollo/client/core';

describe('RssBuilderProductPage', () => {
  let component: RssBuilderProductPage;
  let fixture: ComponentFixture<RssBuilderProductPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        RssBuilderProductPage,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) =>
            mockScrape(apolloMockController),
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerConfigService),
      TestBed.inject(ApolloClient),
    );

    fixture = TestBed.createComponent(RssBuilderProductPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
