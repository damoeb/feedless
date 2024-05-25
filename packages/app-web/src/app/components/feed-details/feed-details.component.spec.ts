import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { FeedDetailsComponent } from './feed-details.component';
import { FeedDetailsModule } from './feed-details.module';
import {
  ApolloMockController,
  AppTestModule,
  mockDocuments,
  mockPlugins,
  mockServerSettings,
} from '../../app-test.module';
import { ServerSettingsService } from '../../services/server-settings.service';
import { ApolloClient } from '@apollo/client/core';

describe('FeedDetailsComponent', () => {
  let component: FeedDetailsComponent;
  let fixture: ComponentFixture<FeedDetailsComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        FeedDetailsModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockPlugins(apolloMockController);
          mockDocuments(apolloMockController);
        }),
      ],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerSettingsService),
      TestBed.inject(ApolloClient),
    );

    fixture = TestBed.createComponent(FeedDetailsComponent);
    component = fixture.componentInstance;
    component.repository = { retention: {}, sources: [], plugins: [] } as any;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
