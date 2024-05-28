import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { AboutPcTrackerPage } from './about-pc-tracker.page';
import {
  ApolloMockController,
  AppTestModule,
  mockRepositories,
  mockServerSettings,
} from '../../../app-test.module';
import { AboutPcTrackerModule } from './about-pc-tracker.module';
import { ServerConfigService } from '../../../services/server-config.service';
import { ApolloClient } from '@apollo/client/core';

describe('AboutPcTrackerPage', () => {
  let component: AboutPcTrackerPage;
  let fixture: ComponentFixture<AboutPcTrackerPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        AboutPcTrackerModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockRepositories(apolloMockController);
        }),
      ],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerConfigService),
      TestBed.inject(ApolloClient),
    );

    fixture = TestBed.createComponent(AboutPcTrackerPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
