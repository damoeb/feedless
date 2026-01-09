import { ComponentFixture, TestBed } from '@angular/core/testing';

import {
  AppTestModule,
  mockPlugins,
  mockRecords,
  mockRepository,
} from '@feedless/testing';
import { FeedBuilderComponent } from './feed-builder.component';
import {
  standaloneV1WebToFeedRoute,
  standaloneV2WebToFeedRoute,
} from '../../router-utils';
import { renderPath, renderQuery } from 'typesafe-routes';
import { GqlExtendContentOptions } from '@feedless/graphql-api';
import { ServerConfigService, SessionService } from '@feedless/services';

describe('FeedBuilderComponent', () => {
  let component: FeedBuilderComponent;
  let fixture: ComponentFixture<FeedBuilderComponent>;
  const mockIsSaasFn = jest.fn<boolean, []>();
  const mockRequestAnonymousFeedToken = jest.fn<string, []>();

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        FeedBuilderComponent,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) => {
            mockPlugins(apolloMockController);
            mockRecords(apolloMockController);
            mockRepository(apolloMockController);
          },
        }),
      ],
      providers: [
        {
          provide: ServerConfigService,
          useValue: {
            apiUrl: 'http://localhost',
            isSaas: mockIsSaasFn,
          },
        },
        {
          provide: SessionService,
          useValue: {
            requestAnonymousFeedToken: mockRequestAnonymousFeedToken,
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedBuilderComponent);
    component = fixture.componentInstance;
    // component.repository = { retention: {}, sources: [], plugins: [] } as any;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('#createFeedUrl', () => {
    it('when sass=false', async () => {
      mockIsSaasFn.mockReturnValue(false);
      component.selectedFeed = {
        genericFeed: {
          selectors: {
            contextXPath: 'contextXPath',
            dateXPath: 'dateXPath',
            linkXPath: 'linkXPath',
            dateIsStartOfEvent: true,
            extendContext: GqlExtendContentOptions.None,
            paginationXPath: 'paginationXPath',
          },
          count: 1,
          score: 1,
          hash: '',
        },
      };
      component.getFilterPlugin = () => ({}) as any;
      const url = await component.createFeedUrl();
      expect(url).toEqual(
        'http://localhost/api/w2f?link=linkXPath&context=contextXPath&date=dateXPath&dateIsEvent=true&q=%7B%7D&out=atom&token=null',
      );
    });
    it('when sass=true', async () => {
      mockIsSaasFn.mockReturnValue(true);
      mockRequestAnonymousFeedToken.mockReturnValue('foo-token');
      component.selectedFeed = {
        genericFeed: {
          selectors: {
            contextXPath: 'contextXPath',
            dateXPath: 'dateXPath',
            linkXPath: 'linkXPath',
            dateIsStartOfEvent: true,
            extendContext: GqlExtendContentOptions.None,
            paginationXPath: 'paginationXPath',
          },
          count: 1,
          score: 1,
          hash: '',
        },
      };
      component.getFilterPlugin = () => ({}) as any;
      const url = await component.createFeedUrl();
      expect(url).toEqual(
        'http://localhost/api/w2f?link=linkXPath&context=contextXPath&date=dateXPath&dateIsEvent=true&q=%7B%7D&out=atom&token=foo-token',
      );
    });
  });

  describe('parse standalone url', () => {
    it('V2', () => {
      const input = {
        url: 'url',
        context: 'context',
        date: 'date',
        out: 'out',
        dateIsEvent: true,
        link: 'link',
        token: 'foo',
        q: 'q',
      };
      const path = renderPath(standaloneV2WebToFeedRoute.feed, input);
      const query = renderQuery(standaloneV2WebToFeedRoute.feed, input);

      const output = component.parseStandaloneUrl(
        `http://localhost/${path}?${query}`,
      );
      expect(output).toEqual(input);
    });

    it('V1', () => {
      const input = {
        url: 'url',
        pContext: 'context',
        pLink: 'link',
      };
      const path = renderPath(standaloneV1WebToFeedRoute.feed, input);
      const query = renderQuery(standaloneV1WebToFeedRoute.feed, input);

      const output = component.parseStandaloneUrl(
        `http://localhost/${path}?${query}`,
      );
      expect(output).toEqual({
        url: 'url',
        context: 'context',
        link: 'link',
        date: '',
        dateIsEvent: false,
      });
    });
  });
});
