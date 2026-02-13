import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FetchRateAccordionComponent } from './fetch-rate-accordion.component';
import {
  ApolloMockController,
  AppTestModule,
  mockServerSettings,
} from '@feedless/testing';
import { ServerConfigService } from '../../services';
import { ApolloClient } from '@apollo/client/core';

describe('FetchRateAccordionComponent', () => {
  let component: FetchRateAccordionComponent;
  let fixture: ComponentFixture<FetchRateAccordionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppTestModule.withDefaults(), FetchRateAccordionComponent],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerConfigService),
      TestBed.inject(ApolloClient),
    );

    fixture = TestBed.createComponent(FetchRateAccordionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
