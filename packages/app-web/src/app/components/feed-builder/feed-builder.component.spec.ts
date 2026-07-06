import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Component, input } from '@angular/core';

import { AppTestModule, mockPlugins, mockRecords, mockRepository } from '../../app-test.module';
import { FeedBuilderComponent } from './feed-builder.component';
import { TransformWebsiteToFeedComponent } from '../transform-website-to-feed/transform-website-to-feed.component';
import { standaloneV1WebToFeedRoute, standaloneV2WebToFeedRoute } from '../../router-utils';
import { renderPath, renderQuery } from 'typesafe-routes';
import { GqlExtendContentOptions } from '../../../generated/graphql';
import { ServerConfigService } from '../../services/server-config.service';
import { SessionService } from '../../services/session.service';
import { IonAccordionGroup } from '@ionic/angular/standalone';

@Component({
  selector: 'app-transform-website-to-feed',
  template: `<ion-accordion-group [multiple]="true"><ng-content select="[beforeFeedsSlot]"></ng-content></ion-accordion-group>`,
  standalone: true,
  imports: [IonAccordionGroup],
})
class StubTransformWebsiteToFeedComponent {
  readonly feed = input();
  readonly sourceBuilder = input();
}

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
        'http://localhost/api/w2f?link=linkXPath&context=contextXPath&date=dateXPath&dateIsEvent=true&q=%7B%7D&out=atom&token=null'
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
        'http://localhost/api/w2f?link=linkXPath&context=contextXPath&date=dateXPath&dateIsEvent=true&q=%7B%7D&out=atom&token=foo-token'
      );
    });
  });

  describe('source accordion helpers', () => {
    it('showTopSearchBar is true before scrape response', () => {
      component.url = 'https://example.com';
      component.sourceBuilder = undefined;
      expect(component.showTopSearchBar()).toBe(true);
    });

    it('showTopSearchBar is false after scrape response', () => {
      component.sourceBuilder = {
        response: { feeds: [] },
      } as any;
      expect(component.showTopSearchBar()).toBe(false);
    });

    it('showTopSearchBar is false when hideSearchBar input is true', () => {
      fixture.componentRef.setInput('hideSearchBar', true);
      fixture.detectChanges();
      expect(component.showTopSearchBar()).toBe(false);
    });

    it('sourceAccordionHighlighted is true when url is set', () => {
      component.url = 'https://example.com';
      expect(component.sourceAccordionHighlighted()).toBe(true);
    });

    it('sourceAccordionHighlighted is true when tags exist', () => {
      component.tags = ['news'];
      expect(component.sourceAccordionHighlighted()).toBe(true);
    });

    it('sourceAccordionHighlighted is true when geoLocation exists', () => {
      component.geoLocation = { lat: 1, lng: 2 };
      expect(component.sourceAccordionHighlighted()).toBe(true);
    });

    it('sourceAccordionHighlighted is false when all source fields empty', () => {
      component.url = '';
      component.tags = [];
      component.geoLocation = undefined;
      component.titleFc.setValue('');
      expect(component.sourceAccordionHighlighted()).toBe(false);
    });
  });

  it('updateSourceUrl preserves sourceBuilder while changing url', async () => {
    const existingBuilder = {
      response: { feeds: [] },
      patchFetch: jest.fn(),
      fetchFeedsUsingStatic: jest.fn().mockResolvedValue(undefined),
    };
    component.sourceBuilder = existingBuilder as any;
    component.url = 'https://old.example';
    component.selectedFeed = { genericFeed: { hash: 'keep-me' } } as any;

    const scrapeUrlSpy = jest.spyOn(component, 'scrapeUrl').mockResolvedValue(undefined);

    await component.updateSourceUrl('https://new.example');

    expect(component.sourceBuilder).toBe(existingBuilder);
    expect(component.url).toBe('https://new.example');
    expect(component.selectedFeed.genericFeed.hash).toBe('keep-me');
    expect(scrapeUrlSpy).toHaveBeenCalled();
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

      const output = component.parseStandaloneUrl(`http://localhost/${path}?${query}`);
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

      const output = component.parseStandaloneUrl(`http://localhost/${path}?${query}`);
      expect(output).toEqual({
        url: 'url',
        context: 'context',
        link: 'link',
        date: '',
        dateIsEvent: false,
      });
    });
  });

  describe('source accordion template', () => {
    beforeEach(async () => {
      await TestBed.resetTestingModule()
        .configureTestingModule({
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
        })
        .overrideComponent(FeedBuilderComponent, {
          remove: { imports: [TransformWebsiteToFeedComponent] },
          add: { imports: [StubTransformWebsiteToFeedComponent] },
        })
        .compileComponents();

      fixture = TestBed.createComponent(FeedBuilderComponent);
      component = fixture.componentInstance;
    });

    it('renders Source accordion with URL and JavaScript inside when response exists', () => {
      component.url = 'https://example.com';
      component.sourceBuilder = {
        response: { feeds: [] },
        needsJavascript: () => false,
        findFirstByPluginsId: () => undefined,
        removePluginById: jest.fn(),
      } as any;
      fixture.detectChanges();

      const compiled = fixture.nativeElement as HTMLElement;
      expect(compiled.textContent).toContain('Source');
      expect(compiled.textContent).not.toContain('Metadata');

      const accordion = compiled.querySelector('ion-accordion[value="source"]');
      expect(accordion).toBeTruthy();
      expect(accordion?.querySelector('.cy-enable-js-button')).toBeTruthy();
      expect(accordion?.querySelector('.cy-searchbar-input')).toBeTruthy();
      expect(compiled.querySelector('.cy-top-url-toolbar')).toBeFalsy();
    });
  });
});
