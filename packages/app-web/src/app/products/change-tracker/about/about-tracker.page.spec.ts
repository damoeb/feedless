import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { AboutTrackerPage } from './about-tracker.page';
import {
  ApolloMockController,
  AppTestModule,
  mockRepositories,
  mockServerSettings,
} from '../../../app-test.module';
import { ServerConfigService } from '../../../services/server-config.service';
import { ApolloClient } from '@apollo/client/core';

describe('AboutPcTrackerPage', () => {
  let component: AboutTrackerPage;
  let fixture: ComponentFixture<AboutTrackerPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        AboutTrackerPage,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) => mockRepositories(apolloMockController),
        }),
      ],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerConfigService),
      TestBed.inject(ApolloClient)
    );

    fixture = TestBed.createComponent(AboutTrackerPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
