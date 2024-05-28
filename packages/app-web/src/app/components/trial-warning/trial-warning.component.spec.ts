import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { TrialWarningComponent } from './trial-warning.component';
import { TrialWarningModule } from './trial-warning.module';
import {
  ApolloMockController,
  AppTestModule,
  mockLicense,
  mockServerSettings,
} from '../../app-test.module';
import { ServerConfigService } from '../../services/server-config.service';
import { ApolloClient } from '@apollo/client/core';

describe('TrialWarningComponent', () => {
  let component: TrialWarningComponent;
  let fixture: ComponentFixture<TrialWarningComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        TrialWarningModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockLicense(apolloMockController);
        }),
      ],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerConfigService),
      TestBed.inject(ApolloClient),
    );

    fixture = TestBed.createComponent(TrialWarningComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
