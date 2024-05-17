import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { GenerateFeedModalComponent } from './generate-feed-modal.component';
import { GenerateFeedModalModule } from './generate-feed-modal.module';
import {
  ApolloMockController,
  AppTestModule,
  mocks,
  mockServerSettings,
} from '../../app-test.module';
import { ServerSettingsService } from '../../services/server-settings.service';
import { ApolloClient } from '@apollo/client/core';

describe('GenerateFeedModalComponent', () => {
  let component: GenerateFeedModalComponent;
  let fixture: ComponentFixture<GenerateFeedModalComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [AppTestModule.withDefaults(), GenerateFeedModalModule],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerSettingsService),
      TestBed.inject(ApolloClient),
    );

    fixture = TestBed.createComponent(GenerateFeedModalComponent);
    component = fixture.componentInstance;
    component.repository = mocks.repository;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
