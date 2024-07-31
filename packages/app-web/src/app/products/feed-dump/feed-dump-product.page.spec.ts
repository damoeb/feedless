import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FeedDumpProductPage } from './feed-dump-product.page';
import {
  ApolloMockController,
  AppTestModule,
  mockScrape,
  mockServerSettings,
} from '../../app-test.module';
import { FeedDumpProductModule } from './feed-dump-product.module';
import { RouterTestingModule } from '@angular/router/testing';
import { ServerConfigService } from '../../services/server-config.service';
import { ApolloClient } from '@apollo/client/core';

describe('FeedDumpProductPage', () => {
  let component: FeedDumpProductPage;
  let fixture: ComponentFixture<FeedDumpProductPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        FeedDumpProductModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockScrape(apolloMockController);
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerConfigService),
      TestBed.inject(ApolloClient),
    );

    fixture = TestBed.createComponent(FeedDumpProductPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
