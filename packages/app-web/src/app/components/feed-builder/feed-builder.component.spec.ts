import { ComponentFixture, TestBed } from '@angular/core/testing';

import {
  AppTestModule,
  mockPlugins,
  mockRecords,
  mockRepository,
} from '../../app-test.module';
import { FeedBuilderComponent } from './feed-builder.component';
import {
  standaloneV1WebToFeedRoute,
  standaloneV2WebToFeedRoute,
} from '../../router-utils';
import { renderPath, renderQuery } from 'typesafe-routes';

describe('FeedBuilderComponent', () => {
  let component: FeedBuilderComponent;
  let fixture: ComponentFixture<FeedBuilderComponent>;

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
    }).compileComponents();

    // await mockServerSettings(
    //   TestBed.inject(ApolloMockController),
    //   TestBed.inject(ServerConfigService),
    //   TestBed.inject(ApolloClient),
    // );

    fixture = TestBed.createComponent(FeedBuilderComponent);
    component = fixture.componentInstance;
    // component.repository = { retention: {}, sources: [], plugins: [] } as any;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
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
        ts: 0,
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
