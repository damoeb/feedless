import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FeedListPage } from './feed-list.page';
import {
  ApolloMockController,
  AppTestModule, mockRepositories,
  mockServerSettings
} from '../../../app-test.module';
import { FeedListModule } from './feed-list.module';
import { ServerSettingsService } from '../../../services/server-settings.service';
import { ApolloClient } from '@apollo/client/core';

describe('FeedListPage', () => {
  let component: FeedListPage;
  let fixture: ComponentFixture<FeedListPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [FeedListModule, AppTestModule.withDefaults(apolloMockController => {
        mockRepositories(apolloMockController)
      })],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerSettingsService),
      TestBed.inject(ApolloClient),
    );

    fixture = TestBed.createComponent(FeedListPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
