import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrialWarningComponent } from './trial-warning.component';
import {
  ApolloMockController,
  AppTestModule,
  mockServerSettings,
} from '@feedless/testing';
import { ServerConfigService } from '../../services';
import { ApolloClient } from '@apollo/client/core';

describe('TrialWarningComponent', () => {
  let component: TrialWarningComponent;
  let fixture: ComponentFixture<TrialWarningComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TrialWarningComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ApolloClient),
    );

    fixture = TestBed.createComponent(TrialWarningComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
