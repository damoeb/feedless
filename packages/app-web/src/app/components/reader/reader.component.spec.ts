import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ReaderComponent } from './reader.component';
import { ReaderModule } from './reader.module';
import { AppTestModule } from '../../app-test.module';
import { GqlFeedlessPlugins } from '../../../generated/graphql';
import { ScrapedReadability } from '../../graphql/types';

describe('ReaderComponent', () => {
  let component: ReaderComponent;
  let fixture: ComponentFixture<ReaderComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [ReaderModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(ReaderComponent);
    component = fixture.componentInstance;
    const readability: ScrapedReadability = {};
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
                name: GqlFeedlessPlugins.OrgFeedlessFulltext,
                value: {
                  one: {
                    mimeType: 'application/json',
                    data: JSON.stringify(readability),
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
