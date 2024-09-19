import { getFirstFetch, SourceBuilder } from './source-builder';
import { ScrapeService } from '../../services/scrape.service';
import {
  GqlFeedlessPlugins,
  GqlPluginExecutionInput,
  GqlScrapeActionInput,
  GqlSourceInput,
} from '../../../generated/graphql';

describe('#getFirstFetch', () => {
  it('given an empty flow returns undefined', () => {
    expect(getFirstFetch([])).toBeUndefined();
  });
  it('given a flow with fetch returns fetch', () => {
    const fetchAction = {
      get: {
        url: {
          literal: '',
        },
      },
    };
    expect(
      getFirstFetch([
        {
          fetch: fetchAction,
        },
      ]),
    ).toEqual(fetchAction);
  });
});

describe('InteractiveWebsiteComponent', () => {
  function createSourceWithActions(
    actions: GqlScrapeActionInput[],
  ): GqlSourceInput {
    return {
      id: '',
      title: '',
      flow: {
        sequence: actions,
      },
    };
  }

  function flowLength(sourceBuilder: SourceBuilder) {
    return sourceBuilder.build().flow.sequence.length;
  }

  const url = '';
  const scrapeService: ScrapeService = {} as any;

  it('fromUrl creates a builder', () => {
    const sourceBuilder = SourceBuilder.fromUrl(url, scrapeService);
    expect(flowLength(sourceBuilder)).toBe(1);
  });

  describe('fromSource', () => {
    it('with invalid source fails', () => {
      expect(() => {
        SourceBuilder.fromSource(createSourceWithActions([]), scrapeService);
      }).toThrow();
    });
    it('creates a builder', () => {
      const sourceBuilder = SourceBuilder.fromSource(
        createSourceWithActions([
          {
            fetch: {
              get: {
                url: {
                  literal: '',
                },
              },
            },
          },
        ]),
        scrapeService,
      );
      expect(flowLength(sourceBuilder)).toBe(1);
    });
  });

  describe('#', () => {
    let sourceBuilder: SourceBuilder;

    beforeEach(() => {
      sourceBuilder = SourceBuilder.fromSource(
        createSourceWithActions([
          {
            fetch: {
              get: {
                url: {
                  literal: url,
                },
              },
            },
          },
          {
            click: {
              position: {
                x: 0,
                y: 0,
              },
            },
          },
          {
            click: {
              position: {
                x: 10,
                y: 10,
              },
            },
          },
          {
            execute: {
              pluginId: GqlFeedlessPlugins.OrgFeedlessFeed,
              params: {},
            },
          },
          {
            execute: {
              pluginId: GqlFeedlessPlugins.OrgFeedlessFilter,
              params: {},
            },
          },
        ]),
        scrapeService,
      );
    });

    it('overwritePostFetchActions', () => {
      const sourceA = sourceBuilder.overwriteFlow([]).build();
      const sourceB = sourceBuilder.overwriteFlow([]).build();

      expect(sourceA).toEqual(sourceB);

      // das isch kaput
      // scrape nach punkt setzen geht nicht
    });

    it('patchFetch', () => {
      expect(sourceBuilder.getUrl()).toEqual(url);

      const newUrl = 'https://example.org';
      sourceBuilder.patchFetch({
        url: {
          literal: newUrl,
        },
        language: 'de',
      });

      expect(sourceBuilder.getUrl()).toEqual(newUrl);
    });

    it('findFirstByPluginsId', () => {
      expect(
        sourceBuilder.findFirstByPluginsId(GqlFeedlessPlugins.OrgFeedlessFeed),
      ).toBeTruthy();
      expect(
        sourceBuilder.findFirstByPluginsId(
          GqlFeedlessPlugins.OrgFeedlessFulltext,
        ),
      ).toBeFalsy();
    });

    it('findAllByPluginsId', () => {
      expect(
        sourceBuilder.findAllByPluginsId(GqlFeedlessPlugins.OrgFeedlessFeed)
          .length,
      ).toBe(1);
      expect(
        sourceBuilder.findAllByPluginsId(GqlFeedlessPlugins.OrgFeedlessFilter)
          .length,
      ).toBe(1);
    });

    describe('addOrUpdatePluginById', () => {
      it('when plugin is present', () => {
        const presentPluginId = GqlFeedlessPlugins.OrgFeedlessFeed;
        const before = sourceBuilder.findFirstByPluginsId(presentPluginId);
        expect(before).toBeTruthy();
        const substitute: GqlPluginExecutionInput = {
          pluginId: presentPluginId,
          params: {
            org_feedless_feed: {},
          },
        };
        sourceBuilder.addOrUpdatePluginById(presentPluginId, {
          execute: substitute,
        });
        const after = sourceBuilder.findFirstByPluginsId(presentPluginId);
        expect(substitute).toEqual(after.execute);
      });

      it('when plugin is not present', () => {
        const notPresentPluginId = GqlFeedlessPlugins.OrgFeedlessFulltext;
        const before = sourceBuilder.findFirstByPluginsId(notPresentPluginId);
        expect(before).toBeFalsy();

        const substitute: GqlPluginExecutionInput = {
          pluginId: notPresentPluginId,
          params: {
            org_feedless_fulltext: {
              inheritParams: false,
              readability: true,
            },
          },
        };
        sourceBuilder.addOrUpdatePluginById(notPresentPluginId, {
          execute: substitute,
        });
        const after = sourceBuilder.findFirstByPluginsId(notPresentPluginId);
        expect(substitute).toEqual(after.execute);
      });
    });

    it('removePluginById', () => {
      const len = flowLength(sourceBuilder);
      expect(
        sourceBuilder.findAllByPluginsId(GqlFeedlessPlugins.OrgFeedlessFeed)
          .length,
      ).toBe(1);
      sourceBuilder.removePluginById(GqlFeedlessPlugins.OrgFeedlessFeed);
      expect(
        sourceBuilder.findAllByPluginsId(GqlFeedlessPlugins.OrgFeedlessFeed)
          .length,
      ).toBe(0);
      expect(flowLength(sourceBuilder)).toBe(len - 1);
    });
  });
});
