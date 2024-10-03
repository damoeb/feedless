import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { FetchRateAccordionComponent } from './fetch-rate-accordion.component';
import { FetchRateAccordionModule } from './fetch-rate-accordion.module';
import {
  ApolloMockController,
  AppTestModule,
  mockServerSettings,
} from '../../app-test.module';
import { ServerConfigService } from '../../services/server-config.service';
import { ApolloClient } from '@apollo/client/core';

describe('GenerateFeedModalComponent', () => {
  let component: FetchRateAccordionComponent;
  let fixture: ComponentFixture<FetchRateAccordionComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [AppTestModule.withDefaults(), FetchRateAccordionModule],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerConfigService),
      TestBed.inject(ApolloClient),
    );

    fixture = TestBed.createComponent(FetchRateAccordionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
