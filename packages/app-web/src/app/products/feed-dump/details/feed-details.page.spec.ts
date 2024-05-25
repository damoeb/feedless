import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FeedDetailsPage } from './feed-details.page';
import { ApolloMockController, AppTestModule, mockServerSettings } from '../../../app-test.module';
import { FeedDetailsModule } from './feed-details.module';
import { ServerSettingsService } from '../../../services/server-settings.service';
import { ApolloClient } from '@apollo/client/core';

describe('FeedDetailsPage', () => {
  let component: FeedDetailsPage;
  let fixture: ComponentFixture<FeedDetailsPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [FeedDetailsModule, AppTestModule.withDefaults()],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerSettingsService),
      TestBed.inject(ApolloClient),
    );

    fixture = TestBed.createComponent(FeedDetailsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
