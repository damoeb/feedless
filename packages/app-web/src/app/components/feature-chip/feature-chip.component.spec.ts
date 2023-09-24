import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { FeatureChipComponent } from './feature-chip.component';
import { FeatureChipModule } from './feature-chip.module';
import {
  ApolloMockController,
  AppTestModule,
  mockServerSettings,
} from '../../app-test.module';
import { ServerSettingsService } from '../../services/server-settings.service';
import { ApolloClient } from '@apollo/client/core';

describe('FeatureChipComponent', () => {
  let component: FeatureChipComponent;
  let fixture: ComponentFixture<FeatureChipComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [FeatureChipModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(FeatureChipComponent);
    component = fixture.componentInstance;
    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerSettingsService),
      TestBed.inject(ApolloClient),
    );
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
