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
      const url = standaloneV2WebToFeedRoute(input).$;
      const output = component.parseStandaloneUrl(
        'http://localhost:8080/' + url,
      );
      expect(output).toEqual(input);
    });
    it('V1', () => {
      const input = {
        url: 'url',
        pContext: 'context',
        dateIsEvent: true,
        pLink: 'link',
      };
      const url = standaloneV1WebToFeedRoute(input).$;
      const output = component.parseStandaloneUrl(
        'http://localhost:8080/' + url,
      );
      expect(output).toEqual({
        url: 'url',
        context: 'context',
        link: 'link',
      });
    });
  });
});
