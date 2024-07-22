import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { TransformWebsiteToFeedComponent } from './transform-website-to-feed.component';
import { ScrapeResponse } from '../../graphql/types';
import { TransformWebsiteToFeedModule } from './transform-website-to-feed.module';
import { AppTestModule, mockRepositories } from '../../app-test.module';

// const jsonFeed = {
//   description: 'Nachrichten nicht nur aus der Welt der Computer',
//   expired: false,
//   feedUrl: 'https://www.heise.de/rss/heise.rdf',
//   items: [
//     {
//       contentRaw:
//         '\u003cp\u003e\u003ca href\u003d"https://www.heise.de/news/China-kann-noch-keine-5-Nanometer-Chips-produzieren-9588363.html?wt_mc\u003drss.red.ho.ho.rdf.beitrag.beitrag"\u003e\u003cimg src\u003d"https://www.heise.de/scale/geometry/450/q80//imgs/18/4/5/2/1/3/5/0/qingyun-l540-light_1-19020858315504cf.jpeg" class\u003d"webfeedsFeaturedVisual" alt\u003d"" /\u003e\u003c/a\u003e\u003c/p\u003e\u003cp\u003eEnde 2023 stellte Huawei ein Notebook mit eigenem 5-nm-Prozessor vor. Der stammt von TSMC aus Taiwan, wie ein Teardown beweist.\u003c/p\u003e',
//       contentRawMime: 'html',
//       contentText:
//         'Ende 2023 stellte Huawei ein Notebook mit eigenem 5-nm-Prozessor vor. Der stammt von TSMC aus Taiwan, wie ein Teardown beweist.',
//       createdAt: 1704459090234,
//       description:
//         'Ende 2023 stellte Huawei ein Notebook mit eigenem 5-nm-Prozessor vor. Der stammt von TSMC aus Taiwan, wie ein Teardown beweist.',
//       id: 'https://www.heise.de/news/China-kann-noch-keine-5-Nanometer-Chips-produzieren-9588363.html?wt_mc\u003drss.red.ho.ho.rdf.beitrag.beitrag',
//       publishedAt: 1704458640000,
//       title: 'China kann noch keine 5-Nanometer-Chips produzieren',
//       url: 'https://www.heise.de/news/China-kann-noch-keine-5-Nanometer-Chips-produzieren-9588363.html?wt_mc\u003drss.red.ho.ho.rdf.beitrag.beitrag',
//     },
//   ],
//   language: 'de',
//   publishedAt: 1704458640000,
//   title: 'heise online News',
//   websiteUrl: 'https://www.heise.de/',
// };
//
// const feeds: GqlScrapedFeeds = {
//   nativeFeeds: [],
//   genericFeeds: [],
// };

const feedResponse: ScrapeResponse = {
  failed: false,
  outputs: [
    {
      fetch: {
        data: '',
        debug: {
          corrId: null,
          console: [],
          cookies: [],
          contentType: 'application/rss+xml; charset=UTF-8',
          statusCode: 200,
          screenshot: null,
          // html: '',
          // metrics: {
          //   queue: 0,
          //   render: 239,
          //   // '__typename': 'ScrapeDebugTimes'
          // },
          viewport: null,
          // '__typename': 'ScrapeDebugResponse'
        },
      },
    },
  ],
};
describe('TransformWebsiteToFeedComponent', () => {
  let component: TransformWebsiteToFeedComponent;
  let fixture: ComponentFixture<TransformWebsiteToFeedComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        TransformWebsiteToFeedModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockRepositories(apolloMockController);
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TransformWebsiteToFeedComponent);
    component = fixture.componentInstance;
    component.scrapeRequest = {
      flow: { sequence: [] },
    };
    component.scrapeResponse = feedResponse;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
