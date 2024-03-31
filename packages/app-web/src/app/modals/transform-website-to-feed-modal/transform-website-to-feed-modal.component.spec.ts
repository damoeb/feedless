import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { TransformWebsiteToFeedModalComponent } from './transform-website-to-feed-modal.component';
import { TransformWebsiteToFeedModalModule } from './transform-website-to-feed-modal.module';
import { AppTestModule } from '../../app-test.module';
import { GqlFeedlessPlugins, GqlScrapedFeeds } from '../../../generated/graphql';

describe('TransformWebsiteToFeedModalComponent', () => {
  let component: TransformWebsiteToFeedModalComponent;
  let fixture: ComponentFixture<TransformWebsiteToFeedModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        TransformWebsiteToFeedModalModule,
        AppTestModule.withDefaults(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TransformWebsiteToFeedModalComponent);
    component = fixture.componentInstance;
    component.scrapeRequest = {
      page: {
        url: '',
      },
      emit: [],
    };
    const feeds: GqlScrapedFeeds = {
      nativeFeeds: [],
      genericFeeds: [],
    };
    component.scrapeResponse = {
      url: '',
      failed: false,
      debug: {
        html: '',
        cookies: [],
        metrics: {
          queue: 0,
          render: 0,
        },
        screenshot: '',
        contentType: '',
        console: [],
        statusCode: 200,
      },
      elements: [
        {
          selector: {
            xpath: { value: '/' },
            fields: [
              {
                name: GqlFeedlessPlugins.OrgFeedlessFeeds,
                value: {
                  one: {
                    mimeType: 'application/json',
                    data: JSON.stringify(feeds),
                  },
                },
              },
            ],
          },
        },
      ],
    };
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
