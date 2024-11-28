import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TransformWebsiteToFeedComponent } from './transform-website-to-feed.component';
import { ScrapeResponse } from '../../graphql/types';
import {
  AppTestModule,
  mockRepositories,
  mockScrape,
} from '../../app-test.module';
import { SourceBuilder } from '../interactive-website/source-builder';
import { ScrapeService } from '../../services/scrape.service';

// const jsonFeed = {
//   description: 'Nachrichten nicht nur aus der Welt der Computer',
//   expired: false,
//   feedUrl: 'https://www.heise.de/rss/heise.rdf',
//   items: [
//     {
//       contentRaw:
//         '\u003cp\u003e\u003ca href\u003d"https://www.heise.de/news/China-kann-noch-keine-5-Nanometer-Chips-produzieren-9588363.html?wt_mc\u003drss.red.ho.ho.rdf.beitrag.beitrag"\u003e\u003cimg src\u003d"https://www.heise.de/scale/geometry/450/q80//imgs/18/4/5/2/1/3/5/0/qingyun-l540-light_1-19020858315504cf.jpeg" class\u003d"webfeedsFeaturedVisual" alt\u003d"" /\u003e\u003c/a\u003e\u003c/p\u003e\u003cp\u003eEnde 2023 stellte Huawei ein Notebook mit eigenem 5-nm-Prozessor vor. Der stammt von TSMC aus Taiwan, wie ein Teardown beweist.\u003c/p\u003e',
//       rawMimeType: 'html',
//       text:
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

describe('TransformWebsiteToFeedComponent', () => {
  let component: TransformWebsiteToFeedComponent;
  let fixture: ComponentFixture<TransformWebsiteToFeedComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        AppTestModule.withDefaults({
          configurer: (apolloMockController) => {
            mockScrape(apolloMockController);
            mockRepositories(apolloMockController);
          },
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TransformWebsiteToFeedComponent);
    component = fixture.componentInstance;
    component.sourceBuilder = SourceBuilder.fromUrl(
      '',
      TestBed.inject(ScrapeService),
    );
    // component.scrapeResponse = feedResponse;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
