import { Component, OnInit, ViewChild } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { GqlScrapeRequestInput } from '../../../generated/graphql';
import { ScrapeService } from '../../services/scrape.service';
import { ScrapeResponse } from '../../graphql/types';
import { NativeOrGenericFeed } from '../../components/transform-website-to-feed/transform-website-to-feed.component';
import { isNull, isUndefined, without } from 'lodash-es';
import { Agent, AgentService } from '../../services/agent.service';
import { KeyLabelOption } from '../../components/select/select.component';
import { ScrapeBuilder } from './scrape-builder';

export function isDefined(v: any | undefined): boolean {
  return !isNull(v) && !isUndefined(v);
}

/**
 *     create feed from website
 *     merge 2 feeds and deduplicate using url/id
 *     use feed and filter title not includes 'Ad'
 *     track pixel page changes of [url], but ship latest text and latest image
 *     track text page changes of [url], but ship diff to first for 2 weeks
 *     track price of product on [url] by extracting field, but shipping product fragment as pixel and markup
 *     use existing feed -> readability, inline images and untrack urls
 *     generate feed, fix title by removing prefix, trim after length 20
 *     inbox: select feeds, filter last 24h, order by quality, pick best 12
 *     digest: select feed, send best 10 end of week as digest via mail
 *     create feed activate tracking
 *     create just the feed sink
 */

interface SourceMapper {
  feed?: NativeOrGenericFeed
  pixel?: string
  textOrMarkup?: string
}

interface PullSource {
  scrapeRequest: GqlScrapeRequestInput,
  scrapeResponse?: ScrapeResponse,
  mapper?: SourceMapper
}

interface ScrapeSourceModalContext {
  request?: GqlScrapeRequestInput
  response?: ScrapeResponse
  fromSpec?: PullSource
}

interface WebsiteToFeedModalContext {
  feed?: NativeOrGenericFeed;
  request: GqlScrapeRequestInput
  response: ScrapeResponse
  fromSpec: PullSource
}

interface WhereSpec {
  type: 'include' | 'exclude';
  field: 'title' | 'description' | 'link'
  negate: '-' | 'not'
  operator: 'contains' | 'endsWith' | 'startsWith'
  value: string
}

class SinkTarget {
  name: () => string
  description: () => string
}

type ThrottleOption = 'all' | 'throttled';

interface SinkSpec {
  targets: SinkTarget[];
  scope: ThrottleOption;
}

function isFeed(contentType: string): boolean {
  return contentType && [
    'application/atom+xml',
    'application/rss+xml',
    'application/xml',
    'text/xml'
  ].some(feedMime => contentType.toLowerCase().startsWith(feedMime));
}

interface FromSpec {
  agent?: Agent;
  pullSources: PullSource[]
  pushTarget?: boolean
}

type OutputType = 'pixel' | 'html' | 'feed' | 'text'

interface FetchSpec {
  frequencyMin?: number
}

type SourceType = 'webhook' | 'url'

@Component({
  selector: 'app-feed-builder',
  templateUrl: './feed-builder-modal.component.html',
  styleUrls: ['./feed-builder-modal.component.scss'],
})
export class FeedBuilderModalComponent implements OnInit {

  @ViewChild('scrapeSourceModal')
  scrapeSourceModalElement: HTMLIonModalElement

  @ViewChild('websiteToFeedModal')
  websiteToFeedModalElement: HTMLIonModalElement

  fromSpec: FromSpec =
    {
      pullSources: []
      // pullSources: [
      //   {
      //     scrapeRequest: {
      //       page: {
      //         url: 'https://heise.de'
      //       },
      //       emit: [GqlScrapeEmitType.Feeds],
      //       elements: ['/'],
      //       debug: {
      //         html: true
      //       }
      //     }
      //   }
      // ],
    };

  whereSpecs: WhereSpec[] = [];

  sinkSpecs: SinkSpec[] = [];

  scrapeSourceModalContext: ScrapeSourceModalContext = {};
  websiteToFeedModalContext: WebsiteToFeedModalContext;
  throttleOptions: KeyLabelOption<ThrottleOption>[] = [
    {key:'all', label: 'All', default: true}, {key:'throttled', label: 'Throttled'}
  ];
  frequencyOptions: KeyLabelOption<number>[] = this.getFetchFrequencyOptions();
  outputOptions: KeyLabelOption<OutputType>[] = [
    {
      key: 'pixel',
      label: 'Pixel',
    },
    {
      key: 'html',
      label: 'Html',
    },
    {
      key: 'text',
      label: 'Text',
    },
    {
      key: 'feed',
      label: 'Feed',
      default: true
    }
  ];
  fetchSpec: FetchSpec = {};
  sourceTypes: KeyLabelOption<SourceType>[] = [{
    key: 'webhook',
    label: 'Webhook (Push)'
  }, {
    key: 'url',
    label: 'Url'
  }];

  builder: ScrapeBuilder = new ScrapeBuilder();

  constructor(readonly modalCtrl: ModalController,
              private readonly scrapeService: ScrapeService,
              private readonly agentService: AgentService) {}

  async ngOnInit() {
    await Promise.all(this.fromSpec.pullSources.filter(source => !source.scrapeResponse)
      .map(source => this.fixSource(source)));
  }

  closeModal() {
    return this.modalCtrl.dismiss();
  }

  getScrapeUrl(fromSpec: PullSource): string {
    return fromSpec.scrapeRequest?.page?.url?.replace(/http[s]?:\/\//, '');
  }

  getNotes(fromSpec: PullSource): string {
    let engine;
    if (fromSpec.scrapeRequest.page.prerender) {
      const actionsCount = fromSpec.scrapeRequest.page.actions?.length || 0;
      engine = `chrome, ${actionsCount} actions`
    } else {
      engine = 'static'
    }

    let responseType: string;
    if (fromSpec.scrapeResponse) {
      responseType = fromSpec.scrapeResponse.debug.contentType.replace(/;.*/,'')
    } else {
      responseType = '...'
    }
    return `${engine} ${responseType}`;
  }

  private async fixSource(source: PullSource) {
    source.scrapeResponse = await this.scrapeService.scrape(source.scrapeRequest)
  }

  async dismissScrapeSourceModal() {
    if (this.scrapeSourceModalContext) {
      const {request, response, fromSpec} = this.scrapeSourceModalContext;
      if (fromSpec) {
        fromSpec.scrapeRequest = request;
        fromSpec.scrapeResponse = response;
      } else {
        this.fromSpec.pullSources.push({
          scrapeRequest: request,
          scrapeResponse: response
        })
      }
    }
    this.scrapeSourceModalContext = null;
    await this.scrapeSourceModalElement.dismiss()
  }

  async openScrapeSourceModal(source?: PullSource) {
    this.scrapeSourceModalContext = {
      request: source?.scrapeRequest,
      response: source?.scrapeResponse,
      fromSpec: source
    }
    await this.scrapeSourceModalElement.present()
  }

  async dismissWebsiteToFeedModal() {
    await this.websiteToFeedModalElement.dismiss()
    if (this.websiteToFeedModalContext) {
      const {  fromSpec, feed } = this.websiteToFeedModalContext;
      if (!fromSpec.mapper) {
        fromSpec.mapper = {}
      }
      fromSpec.mapper.feed = feed;
    }
    this.websiteToFeedModalContext = null;
  }

  async openResourceMapperModal(fromSpec: PullSource) {
    if (this.builder.select.output().includes('feed')) {
      return this.openWebsiteToFeedModal(fromSpec);
    } else {
      return this.openFragmentExtractorModal(fromSpec);
    }
  }

  private async openWebsiteToFeedModal(fromSpec: PullSource) {
    this.websiteToFeedModalContext = {
      request: fromSpec.scrapeRequest,
      response: fromSpec.scrapeResponse,
      fromSpec
    }
    await this.websiteToFeedModalElement.present()
  }

  getLabelForResourceMapper({ mapper }: PullSource): string {
    if (this.builder.select.output().includes('feed')) {
      if (mapper.feed.genericFeed) {
        return 'Generic Feed';
      } else if (mapper.feed.nativeFeed) {
        return 'Native Feed';
      } else {
        throw new Error('not supported')
      }
    } else {
      return 'Fragment Extractor'
    }
  }

  deleteFromSpec(source: PullSource) {
    this.fromSpec.pullSources = without(this.fromSpec.pullSources, source);
  }

  addWhereSpec() {
    this.whereSpecs.push({
      field: 'title',
      negate: '-',
      operator: 'contains',
      value: '',
      type: 'exclude'
    });
  }

  private getFetchFrequencyOptions(): KeyLabelOption<number>[] {
    const hour = 60;
    const day = 24 * hour;
    return [
      {
        key: hour,
        label: 'Every hour'
      },
      {
        key: 2 * hour,
        label: 'Every 2 hours'
      },
      {
        key: 4 * hour,
        label: 'Every 4 hours',
        default: true
      },
      {
        key: 8 * hour,
        label: 'Every 8 hours'
      },
      {
        key: 12 * hour,
        label: 'Every 12 hours'
      },
      {
        key: day,
        label: 'Every day'
      },
      {
        key: 2 * day,
        label: 'Every 2 days'
      },
      {
        key: 7 * day,
        label: 'Every week'
      },
      {
        key: 28 * day,
        label: 'Every month'
      }
    ];
  }

  build() {

  }

  deleteWhereSpec(whereSpec: WhereSpec) {
    this.whereSpecs = without(this.whereSpecs, whereSpec);
  }

  needsSourceConverter(fromSpec: PullSource): boolean {
    return this.builder.select.hasPicked('pixel') ||
    this.builder.select.hasPicked('feed') && fromSpec.scrapeResponse && !isFeed(fromSpec.scrapeResponse.debug.contentType)
  }

  needsAgents(): boolean {
    return this.builder.select.hasPicked('pixel') ||
      this.fromSpec.pullSources.some(source => !!source.scrapeRequest?.page?.prerender);
  }

  getAgents(): Agent[] {
    const agents = this.agentService.getAgents()
    return [
      ...agents,
      // {
      //   label: 'Create new agent...',
      //   value: 'new'
      // }
    ];
  }

  dismissFeedModal() {

  }

  getAgentLabelProvider(agent: Agent) {
    return agent.name;
  }

  hasProperResourceMapper(source: PullSource) {
    if (this.builder.select.hasPicked('feed')) {
      return isDefined(source.mapper?.feed);
    }
    // switch (this.selectSpec) {
    //   case 'feed': return isDefined(source.mapper?.feed);
    //   case 'pixel': return isDefined(source.mapper?.pixel);
    //   case 'text': return isDefined(source.mapper?.textOrMarkup);
    //   case 'html': return isDefined(source.mapper?.textOrMarkup);
    // }
    return false;
  }

  async handleSourceType(key: SourceType) {
    if (key === 'url') {
      await this.openScrapeSourceModal()
    } else {
      if (key === 'webhook') {
        this.fromSpec.pushTarget = true
      }
    }
  }

  private openFragmentExtractorModal(fromSpec: PullSource) {
    return Promise.resolve(undefined);
  }
}
