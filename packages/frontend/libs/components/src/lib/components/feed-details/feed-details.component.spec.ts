import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FeedDetailsComponent } from './feed-details.component';
import {
  ApolloMockController,
  AppTestModule,
  mockPlugins,
  mockRecords,
  mockRepository,
  mockServerSettings,
} from '@feedless/testing';
import { ServerConfigService } from '../../services';
import { ApolloClient } from '@apollo/client/core';

describe('FeedDetailsComponent', () => {
  let component: FeedDetailsComponent;
  let fixture: ComponentFixture<FeedDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        FeedDetailsComponent,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) => {
            mockPlugins(apolloMockController);
            mockRecords(apolloMockController);
            mockRepository(apolloMockController);
          },
        }),
      ],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerConfigService),
      TestBed.inject(ApolloClient),
    );

    fixture = TestBed.createComponent(FeedDetailsComponent);
    component = fixture.componentInstance;
    const componentRef = fixture.componentRef;
    componentRef.setInput('repository', {
      retention: {},
      sources: [],
      plugins: [],
    } as any);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
