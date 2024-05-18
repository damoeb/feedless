import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FeedTilesPage } from './feed-tiles.page';
import {
  ApolloMockController,
  AppTestModule,
  mockRepositories,
  mockServerSettings,
} from '../../../app-test.module';
import { FeedTilesModule } from './feed-tiles.module';
import { ServerSettingsService } from '../../../services/server-settings.service';
import { ApolloClient } from '@apollo/client/core';

describe('FeedListPage', () => {
  let component: FeedTilesPage;
  let fixture: ComponentFixture<FeedTilesPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        FeedTilesModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockRepositories(apolloMockController);
        }),
      ],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerSettingsService),
      TestBed.inject(ApolloClient),
    );

    fixture = TestBed.createComponent(FeedTilesPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
