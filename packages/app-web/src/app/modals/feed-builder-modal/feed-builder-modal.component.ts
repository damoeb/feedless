import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit, ViewChild } from '@angular/core';
import { GqlScrapeEmitType } from '../../../generated/graphql';
import { NativeOrGenericFeed } from '../../components/transform-website-to-feed/transform-website-to-feed.component';
import { isNull, isUndefined } from 'lodash-es';
import { Agent, AgentService } from '../../services/agent.service';
import { ResponseMapper, ScrapeBuilder, ScrapeBuilderSpec, SinkSpec, SinkTargetSpec, SourceBuilder } from './scrape-builder';
import { ScrapeService } from '../../services/scrape.service';
import { debounce, interval } from 'rxjs';
import { KeyLabelOption } from '../../components/select/select.component';
import { ModalController } from '@ionic/angular';
import { ImporterService } from '../../services/importer.service';


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

type RefineType = 'create' | 'update'

type SinkTarget = 'email' | 'webhook' | 'feed'

type FeedType = 'existing' | 'new'

type SinkScope = 'scoped' | 'unscoped'

export interface FeedBuilderCardComponentProps {
  scrapeBuilderSpec: ScrapeBuilderSpec
}

@Component({
  selector: 'app-feed-builder',
  templateUrl: './feed-builder-modal.component.html',
  styleUrls: ['./feed-builder-modal.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FeedBuilderModalComponent implements OnInit, FeedBuilderCardComponentProps {

  @Input({required: true})
  scrapeBuilderSpec: ScrapeBuilderSpec

  @ViewChild('scrapeSourceModal')
  scrapeSourceModalElement: HTMLIonModalElement

  @ViewChild('websiteToFeedModal')
  websiteToFeedModalElement: HTMLIonModalElement

  @ViewChild('agentModal')
  agentModalElement: HTMLIonModalElement

  builder: ScrapeBuilder;
  agents: Agent[] = [];

  scrapeSourceModalContext: ScrapeSourceModalContext;
  websiteToFeedModalContext: WebsiteToFeedModalContext;
  fieldRefineOptions: KeyLabelOption<RefineType>[] = [
    {
      key: 'create',
      label: 'Create Field'
    },
    {
      key: 'update',
      label: 'Modify Field'
    }
  ];
  sinkTargetOptions: KeyLabelOption<SinkTarget>[] = [
    {
      key: 'email',
      label: 'Email'
    },
    {
      key: 'webhook',
      label: 'Webhook'
    },
    {
      key: 'feed',
      label: 'Feed'
    },
    ];
  fetchFrequencyOptions = this.getFetchFrequencyOptions()
  feedTypeOptions: KeyLabelOption<FeedType>[] = [
    {
      key: 'existing',
      label: 'Existing Feed'
    },
    {
      key: 'new',
      label: 'New Feed'
    }
  ];

  sinkScopeOptions: KeyLabelOption<SinkScope>[] = [
    {
      key: 'scoped',
      label: 'Scoped'
    },
    {
      key: 'unscoped',
      label: 'Everything',
      default: true
    }
  ]
  timeSegments: KeyLabelOption<number>[] = this.getTimeSegmentsOptions();

  constructor(private readonly scrapeService: ScrapeService,
              private readonly changeRef: ChangeDetectorRef,
              private readonly importerService: ImporterService,
              private readonly modalCtrl: ModalController,
              private readonly agentService: AgentService) {
  }

  async ngOnInit(): Promise<void> {
    this.builder = new ScrapeBuilder(this.scrapeService, this.scrapeBuilderSpec);
    this.builder.valueChanges
      .pipe(debounce(() => interval(50)))
      .subscribe(() => {
        this.changeRef.detectChanges();
      })
    this.agents = await this.agentService.getAgents()
  }

  addSource() {
    const sourceBuilder = this.builder.sources.add();
    return this.openScrapeSourceModal(sourceBuilder)
  }

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

  async applyChangesFromScrapeSourceModal() {
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

  openMapperModal(source: SourceBuilder, responseMapper: ResponseMapper = null) {
    switch (responseMapper || source.getResponseMapperType()) {
      case 'feed':
        return this.openWebsiteToFeedModal(source);
      case 'fragment':
        return this.openScrapeSourceModal(source, true)
    }
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

  private getTimeSegmentsOptions(): KeyLabelOption<number>[] {
    const hour = 60;
    const day = 24 * hour;
    const week = 7 * day;
    return [
      {
        key: 24 * hour,
        label: '24h'
      },
      {
        key: 7 * day,
        label: 'Last week',
        default: true
      },
      {
        key: 4 * week,
        label: 'Last month',
      },
    ];
  }

  private async openWebsiteToFeedModal(sourceBuilder: SourceBuilder) {
    this.websiteToFeedModalContext = {
      sourceBuilder
    }
    await this.websiteToFeedModalElement.present()
  }
  async applyChangesFromebsiteToFeedModal() {
    await this.websiteToFeedModalElement.dismiss()
    const {  feed } = this.websiteToFeedModalContext;
    this.websiteToFeedModalContext.sourceBuilder.withMapper({
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

  addFieldRefinement(option: KeyLabelOption<RefineType>) {
    switch (option.key) {
      case 'create': return this.builder.addRefine({
        create: {
        }
      });
      case 'update': return this.builder.addRefine({
        update: {
        }
      });
      default: throw new Error('not supported')
    }
  }

  isDefined(v: any | undefined): boolean {
    return !isNull(v) && !isUndefined(v);
  }

  createSink(option: KeyLabelOption<SinkTarget>) {
    this.builder.addSink({
      targets: [
        this.getDefaultForSinkTarget(option.key)
      ]
    })
  }

  private getDefaultForSinkTarget(target: SinkTarget): SinkTargetSpec {
    switch (target) {
      case 'feed': return {
        feed: {}
      };
      case 'webhook': return {
        webhook: {
          url: ''
        }
      };
      case 'email': return {
        email: {
          address: ''
        }
      };
      default: throw new Error('not supported')
    }
  }

  closeModal() {
    this.modalCtrl.dismiss()
  }

  handleFeedType(type: FeedType, sink: SinkTargetSpec) {
    switch (type) {
      case 'new':
        sink.feed.create = {}
        break;
      case 'existing':
        sink.feed.existing = {}
        break;
      default: throw new Error('not supported')
    }
  }

  addTargetToSink(target: SinkTarget, sink: SinkSpec) {
    sink.targets.push(this.getDefaultForSinkTarget(target))
  }

  handleSinkScopeChange(scope: SinkScope, spec: SinkSpec) {
    switch (scope) {
      case 'scoped':
        spec.scoped = {
        }
        break;
      case 'unscoped':
        spec.scoped = null;
        break;
      default: throw new Error('not supported')
    }
  }

  save() {
    const ff= this.builder.build();
    console.log(JSON.stringify(ff, null, 2))
  }
}
