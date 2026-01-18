import { EventEmitter } from '@angular/core';
import {
  GqlFeedlessPlugins,
  GqlHttpFetch,
  GqlHttpFetchInput,
  GqlHttpGetRequestInput,
  GqlScrapeActionInput,
  GqlScrapeEmit,
  GqlScrapeFlowInput,
  GqlSource,
  GqlSourceInput,
  ScrapeResponse,
} from '@feedless/graphql-api';
import { ReplaySubject } from 'rxjs';
// eslint-disable-next-line @nx/enforce-module-boundaries
import { ScrapeService } from '@feedless/services';
import { cloneDeep, first, pick } from 'lodash-es';
import { FormControl, FormGroup } from '@angular/forms';
import { BoundingBox, isDefined, XyPosition } from '@feedless/core';

export type ScrapeControllerState = 'DIRTY' | 'PRISTINE';

function assertTrue(
  condition: boolean,
  error: string,
  flow: GqlScrapeActionInput[],
) {
  if (!condition) {
    throw new Error(`${error}, flow=${JSON.stringify(flow, null, 2)}`);
  }
}

export function getFirstFetchUrlLiteral(
  actions: { fetch?: GqlHttpFetch | GqlHttpFetchInput }[],
): string {
  return getFirstFetch(actions)?.get?.url?.literal;
}

export function getFirstFetch(
  actions: { fetch?: GqlHttpFetch | GqlHttpFetchInput }[],
): GqlHttpFetch {
  const fetchList = actions.filter((action) => isDefined(action.fetch));
  if (fetchList.length > 0) {
    return fetchList[0].fetch as GqlHttpFetch;
  }
  throw new Error('No fetch action found');
}

export class SourceBuilder {
  events = {
    pickPoint: new EventEmitter<(position: XyPosition) => void>(),
    pickElement: new EventEmitter<(xpath: string) => void>(),
    pickArea: new EventEmitter<(bbox: BoundingBox) => void>(),
    actionsChanges: new EventEmitter<void>(),
    extractElements: new EventEmitter<{
      xpath: string;
      callback: (elements: HTMLElement[]) => void;
    }>(),
    showElements: new ReplaySubject<string>(),
    stateChange: new ReplaySubject<ScrapeControllerState>(),
    cancel: new EventEmitter<void>(),
  };

  response: ScrapeResponse;
  private flow: GqlScrapeActionInput[] = [];

  meta = new FormGroup({
    title: new FormControl<string>(''),
    tags: new FormControl<string[]>([]),
    latLng: new FormControl<GqlSourceInput['latLng']>(null),
  });

  static fromSource(
    source: GqlSourceInput,
    scrapeService: ScrapeService,
  ): SourceBuilder {
    return new SourceBuilder(scrapeService, source);
  }

  static fromUrl(url: string, scrapeService: ScrapeService) {
    const source = {
      title: `From ${url}`,
      tags: [] as string[],
      flow: {
        sequence: [
          {
            fetch: {
              get: {
                url: {
                  literal: url,
                },
              },
            },
          },
        ],
      },
    };

    return new SourceBuilder(scrapeService, source);
  }

  private constructor(
    private scrapeService: ScrapeService,
    source: GqlSourceInput,
  ) {
    this.patch(pick(source, ['latLng', 'tags', 'title']));
    this.flow = cloneDeep(source.flow.sequence);
    this.validateFlow();
  }

  getUrl() {
    return getFirstFetchUrlLiteral(this.flow);
  }

  patchFetch(
    params: Partial<
      Pick<
        GqlHttpGetRequestInput,
        | 'additionalWaitSec'
        | 'url'
        | 'timeout'
        | 'language'
        | 'forcePrerender'
        | 'viewport'
      >
    >,
  ) {
    const fetchAction = getFirstFetch(this.flow);

    Object.keys(params).forEach((key) => (fetchAction.get[key] = params[key]));
    this.events.stateChange.next('DIRTY');
    return this;
  }

  patch(
    param: Partial<Pick<GqlSourceInput, 'tags' | 'latLng' | 'title' | 'draft'>>,
  ) {
    this.meta.patchValue(param);
    this.events.stateChange.next('DIRTY');
  }

  build(append: Array<GqlScrapeActionInput> = null): GqlSource {
    console.log(this.flow);
    return this.toSource({
      sequence: [...this.flow, ...(append ? append : [])],
    }) as GqlSource;
  }

  removePluginById(pluginId: GqlFeedlessPlugins) {
    if (this.flow) {
      this.flow = this.flow.filter((a) => a.execute?.pluginId !== pluginId);
      this.events.stateChange.next('DIRTY');
    }
    this.validateFlow();
    return this;
  }

  addOrUpdatePluginById(
    pluginId: GqlFeedlessPlugins,
    action: GqlScrapeActionInput,
    before: GqlFeedlessPlugins = null,
  ) {
    console.log('updatePluginById', pluginId, this.flow);

    const updateIndex = this.flow.findIndex(
      (a) => a.execute?.pluginId === pluginId,
    );
    if (updateIndex > -1) {
      console.log(`update ${pluginId} #${updateIndex}`);
      this.flow[updateIndex] = action;
    } else {
      console.log(`add ${pluginId}`);

      if (before) {
        const addBeforeIndex = this.flow.findIndex(
          (a) => a.execute?.pluginId === before,
        );
        if (addBeforeIndex > -1) {
          this.flow = [
            ...this.flow.slice(0, addBeforeIndex),
            action,
            ...this.flow.slice(addBeforeIndex),
          ];
        } else {
          this.flow.push(action);
        }
      } else {
        this.flow.push(action);
      }
    }
    this.validateFlow();
    this.events.stateChange.next('DIRTY');
    return this;
  }

  findFirstByPluginsId(pluginId: GqlFeedlessPlugins): GqlScrapeActionInput {
    return first(this.findAllByPluginsId(pluginId));
  }

  findAllByPluginsId(pluginId: GqlFeedlessPlugins): GqlScrapeActionInput[] {
    return this.flow.filter((a) => a.execute?.pluginId === pluginId);
  }

  hasFetchActionReturnedHtml() {
    const contentType = this.response?.outputs?.find((o) => o.response.fetch)
      ?.response?.fetch?.debug?.contentType;
    return contentType?.toLowerCase()?.startsWith('text/html');
  }

  overwriteFlow(flow: GqlScrapeActionInput[]) {
    console.log('overwritePostFetchActions from -> ', this.flow);
    this.flow = flow;
    console.log('overwritePostFetchActions to -> ', this.flow);
    return this;
  }

  async fetchFeedsUsingStatic() {
    return this.fetchFeeds('fetchFeedsFromStatic', []);
  }

  async fetchFeedsUsingBrowser() {
    return this.fetchFeeds('fetchFeedsFromBrowser', [
      {
        extract: {
          fragmentName: 'full-page',
          selectorBased: {
            fragmentName: '',
            xpath: {
              value: '/',
            },
            uniqueBy: GqlScrapeEmit.Html,
            emit: [GqlScrapeEmit.Html, GqlScrapeEmit.Text, GqlScrapeEmit.Pixel],
          },
        },
      },
    ]);
  }

  // async fetchUsingBrowser() {
  //   return this.fetchFeeds('fetchFeedsFromBrowser', [{
  //     extract: {
  //       fragmentName: 'full-page',
  //       selectorBased: {
  //         fragmentName: '',
  //         xpath: {
  //           value: '/',
  //         },
  //         emit: [GqlScrapeEmit.Html, GqlScrapeEmit.Text, GqlScrapeEmit.Pixel],
  //       },
  //     },
  //   }]);
  // }

  private async fetchFeeds(title: string, actions: GqlScrapeActionInput[]) {
    return this.fetch(title, [
      ...actions,
      {
        execute: {
          pluginId: GqlFeedlessPlugins.OrgFeedlessFeeds,
          params: {},
        },
      },
    ]);
  }

  private async fetch(title: string, actions: GqlScrapeActionInput[]) {
    console.log(
      'fetchFeeds',
      this.flow,
      '->',
      this.flow.filter((a) => !isDefined(a.execute)),
    );

    this.validateFlow();
    this.response = await this.scrapeService.scrape(
      this.toSource(
        {
          sequence: [
            ...this.flow.filter((a) => !isDefined(a.execute)),
            ...actions,
          ],
        },
        title,
      ),
    );
    this.events.stateChange.next('DIRTY');
    return this.response;
  }

  private toSource(
    flow: GqlScrapeFlowInput,
    title: string = null,
  ): GqlSourceInput {
    return {
      id: '',
      title: title || this.meta.value.title,
      latLng: this.meta.value.latLng,
      tags: this.meta.value.tags,
      flow,
    };
  }

  private validateFlow() {
    const fetchActionCount = this.flow.filter((a) => isDefined(a.fetch)).length;
    assertTrue(
      fetchActionCount === 1,
      `Invalid number of fetch actions ${fetchActionCount}`,
      this.flow,
    );

    assertTrue(
      this.findAllByPluginsId(GqlFeedlessPlugins.OrgFeedlessFeed).length <= 1,
      'too many feed plugins',
      this.flow,
    );
    assertTrue(
      this.findAllByPluginsId(GqlFeedlessPlugins.OrgFeedlessFeeds).length <= 1,
      'too many feeds plugins',
      this.flow,
    );
  }

  needsJavascript() {
    return getFirstFetch(this.flow)?.get?.forcePrerender;
  }
}

export function findAllByPluginsIdIn(
  source: GqlSourceInput,
  pluginIds: GqlFeedlessPlugins[],
): GqlScrapeActionInput[] {
  return source.flow.sequence.filter((a) =>
    pluginIds.includes(a.execute?.pluginId as any),
  );
}

export function findAllByPluginsIdNotIn(
  source: GqlSourceInput,
  pluginIds: GqlFeedlessPlugins[],
): GqlScrapeActionInput[] {
  return source.flow.sequence.filter(
    (a) => !pluginIds.includes(a.execute?.pluginId as any),
  );
}
