import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FeedBuilderActionsModalComponent } from './feed-builder-actions-modal.component';
import {
  ApolloMockController,
  AppTestModule,
  mockServerSettings,
} from '../../app-test.module';
import { FeedBuilderActionsModalModule } from './feed-builder-actions-modal.module';
import { ServerConfigService } from '../../services/server-config.service';
import { ApolloClient } from '@apollo/client/core';

describe('FeedBuilderActionsModalComponent', () => {
  let component: FeedBuilderActionsModalComponent;
  let fixture: ComponentFixture<FeedBuilderActionsModalComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [FeedBuilderActionsModalModule, AppTestModule.withDefaults()],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerConfigService),
      TestBed.inject(ApolloClient),
    );

    fixture = TestBed.createComponent(FeedBuilderActionsModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
