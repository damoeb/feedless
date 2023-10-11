import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit, ViewChild } from '@angular/core';
import { GqlScrapeEmitType } from '../../../generated/graphql';
import { NativeOrGenericFeed } from '../../components/transform-website-to-feed/transform-website-to-feed.component';
import { isNull, isUndefined } from 'lodash-es';
import { Agent, AgentService } from '../../services/agent.service';
import { ResponseMapper, ScrapeBuilder, ScrapeBuilderSpec, SourceBuilder } from './scrape-builder';
import { ScrapeService } from '../../services/scrape.service';
import { debounce, interval } from 'rxjs';
import { KeyLabelOption } from '../../components/select/select.component';

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


interface ScrapeSourceModalContext {
  sourceBuilder: SourceBuilder
  pickFragment: boolean
}

interface WebsiteToFeedModalContext {
  feed?: NativeOrGenericFeed;
  sourceBuilder: SourceBuilder
}


type ThrottleOption = 'all' | 'throttled';

@Component({
  selector: 'app-feed-builder',
  templateUrl: './feed-builder-modal.component.html',
  styleUrls: ['./feed-builder-modal.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FeedBuilderModalComponent implements OnInit {

  @ViewChild('scrapeSourceModal')
  scrapeSourceModalElement: HTMLIonModalElement

  @ViewChild('websiteToFeedModal')
  websiteToFeedModalElement: HTMLIonModalElement

  @ViewChild('agentModal')
  agentModalElement: HTMLIonModalElement

  builder: ScrapeBuilder;
  agents: Agent[] = [];

  constructor(readonly scrapeService: ScrapeService,
              private readonly changeRef: ChangeDetectorRef,
              private readonly agentService: AgentService) {
    const config: ScrapeBuilderSpec = {
      sources: [
        {
          request: {
              page: {
                url: 'https://heise.de'
              },
              emit: [GqlScrapeEmitType.Feeds],
              elements: ['/'],
              debug: {
                html: true,
              }
          },
        },
      ]
    }
    this.builder = new ScrapeBuilder(scrapeService, config)
    this.builder.valueChanges
      .pipe(debounce(() => interval(50)))
      .subscribe(() => {
      this.changeRef.detectChanges();
    })
  }

  async ngOnInit(): Promise<void> {
    this.agents = await this.agentService.getAgents()
  }

  addSource() {
    const sourceBuilder = this.builder.sources.add();
    return this.openScrapeSourceModal(sourceBuilder)
  }

  scrapeSourceModalContext: ScrapeSourceModalContext;
  websiteToFeedModalContext: WebsiteToFeedModalContext;
  throttleOptions: KeyLabelOption<ThrottleOption>[] = [
    {key:'all', label: 'All', default: true}, {key:'throttled', label: 'Throttled'}
  ]

  getRequestLabel(source: SourceBuilder) {
    return source.request?.page?.url?.replace(/http[s]?:\/\//, '');
  }

  getResponseLabel(source: SourceBuilder) {
    let engine;
    if (source.request?.page?.prerender) {
      const actionsCount = source.request?.page?.actions?.length || 0;
      engine = `chrome, ${actionsCount} actions`
    } else {
      engine = 'static'
    }

    let responseType: string;
    if (source.response) {
      responseType = source.response.debug.contentType.replace(/;.*/,'')
    } else {
      responseType = '...'
    }
    return `${engine} ${responseType}`;
  }

  async dismissScrapeSourceModal() {
    this.scrapeSourceModalContext = null;
    await this.scrapeSourceModalElement.dismiss()
  }

  async openScrapeSourceModal(sourceBuilder: SourceBuilder, pickFragment: boolean = false) {
    this.scrapeSourceModalContext = {
      sourceBuilder,
      pickFragment
    }
    await this.scrapeSourceModalElement.present()
  }

  setMapper(source: SourceBuilder, responseMapper: ResponseMapper) {
    switch (responseMapper) {
      case 'feed':
        return this.openWebsiteToFeedModal(source);
      case 'fragment':
        return this.openScrapeSourceModal(source, true)
    }
  }

  private async openWebsiteToFeedModal(sourceBuilder: SourceBuilder) {
    this.websiteToFeedModalContext = {
      sourceBuilder
    }
    await this.websiteToFeedModalElement.present()
  }

  async dismissWebsiteToFeedModal() {
    await this.websiteToFeedModalElement.dismiss()
    const {  feed } = this.websiteToFeedModalContext;
    this.websiteToFeedModalContext.sourceBuilder.mapper.withMapper({
      feed
    });
    this.websiteToFeedModalContext = null;
    console.log(JSON.stringify(this.builder.build(), null, 2))
  }

  async createAgent() {
    await this.websiteToFeedModalElement.present()
  }

  labelAgent(agent: Agent) {
    return `${agent.version} - ${agent.osInfo}`;
  }

  print() {
    console.log(JSON.stringify(this.builder.build(), null, 2))
  }

  getLabelForSource(source: SourceBuilder) {
    return source.produces()
      .filter(it => it)
      .map(it => it.label).join(', ') || '...'
  }

  fields() {
    return this.builder.produces().flatMap(a => a.fields);
  }
}
