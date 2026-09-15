import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FeedDetailsComponent, repositoryFeedUrl } from './feed-details.component';
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
import { GqlVisibility } from '@feedless/graphql-api';

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

describe('repositoryFeedUrl', () => {
  const repository = (visibility: GqlVisibility, shareKey: string) => ({
    id: 'repo-1',
    visibility,
    shareKey,
  });

  it('adds the share key of a private repository to the feed and iCal URL', () => {
    const privateRepo = repository(GqlVisibility.IsPrivate, 'key-1');
    expect(repositoryFeedUrl('https://api', privateRepo, 'atom')).toEqual(
      'https://api/f/repo-1/atom?skey=key-1',
    );
    expect(repositoryFeedUrl('https://api', privateRepo, 'cal')).toEqual(
      'https://api/f/repo-1/cal?skey=key-1',
    );
  });

  it('leaves the share key out of the feed and iCal URL of a public repository', () => {
    const publicRepo = repository(GqlVisibility.IsPublic, 'key-1');
    expect(repositoryFeedUrl('https://api', publicRepo, 'atom')).toEqual(
      'https://api/f/repo-1/atom',
    );
    expect(repositoryFeedUrl('https://api', publicRepo, 'cal')).toEqual(
      'https://api/f/repo-1/cal',
    );
  });

  it('leaves the share key out when a private repository has none', () => {
    const privateRepo = repository(GqlVisibility.IsPrivate, '');
    expect(repositoryFeedUrl('https://api', privateRepo, 'cal')).toEqual(
      'https://api/f/repo-1/cal',
    );
  });
});
