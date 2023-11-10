import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit, ViewChild } from '@angular/core';
import { GqlAgentInput, GqlScrapeEmitType, GqlScrapeRequestInput } from '../../../generated/graphql';
import { NativeOrGenericFeed } from '../../components/transform-website-to-feed/transform-website-to-feed.component';
import { cloneDeep, isNull, isUndefined } from 'lodash-es';
import { Agent, AgentService } from '../../services/agent.service';
import {
  Field, isDefined,
  ResponseMapper,
  ScrapeBuilder,
  ScrapeBuilderSpec,
  SegmentedOutputSpec,
  SinkSpec,
  SinkTargetSpec,
  SourceBuilder
} from './scrape-builder';
import { ScrapeService } from '../../services/scrape.service';
import { debounce, interval } from 'rxjs';
import { KeyLabelOption } from '../../components/select/select.component';
import { ModalController } from '@ionic/angular';
import { ImporterService } from '../../services/importer.service';
import { ScrapeResponse } from '../../graphql/types';

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
  request: GqlScrapeRequestInput;
  response?: ScrapeResponse;
  sourceBuilder: SourceBuilder;
}

type RefineType = 'create' | 'update'

type SinkTarget = 'email' | 'webhook'

type FeedType = 'existing' | 'new'

type SinkScope = 'segmented' | 'unscoped'

export interface FeedBuilderCardComponentProps {
  scrapeBuilderSpec: ScrapeBuilderSpec
}

interface SegmentedDeliveryModalContext {
  segmented: SegmentedOutputSpec
}

// type FeedBuilder = {
//   sources: Source[]
//   agent: GqlAgentInput
// }

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

  @ViewChild('segmentedDeliveryModal')
  segmentedDeliveryModalElement: HTMLIonModalElement

  @ViewChild('agentModal')
  agentModalElement: HTMLIonModalElement

  builder: ScrapeBuilder;
  agents: Agent[] = [];

  scrapeSourceModalContext: ScrapeSourceModalContext;
  segmentedDeliveryModalContext: SegmentedDeliveryModalContext;
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
      key: 'segmented',
      label: 'Scoped'
    },
    {
      key: 'unscoped',
      label: 'Everything',
      default: true
    }
  ]
  timeSegments: KeyLabelOption<number>[] = this.getTimeSegmentsOptions();
  fields: Field[];
  hasFields: boolean;

  // formGroup: FormGroup<TypedFormControls<FeedBuilder>>

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

        this.fields = this.builder.produces().flatMap(a => a.fields)
        this.hasFields = this.fields.length == 0;
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

  // -- scrape modal -----------------------------------------------------------

  async openScrapeSourceModal(sourceBuilder: SourceBuilder) {
    this.scrapeSourceModalContext = {
      request: cloneDeep(sourceBuilder.request),
      response: cloneDeep(sourceBuilder.response),
      sourceBuilder,
    }
    await this.scrapeSourceModalElement.present()
  }

  async dismissScrapeSourceModal() {
    this.scrapeSourceModalContext = null;
    await this.scrapeSourceModalElement.dismiss()
  }
  async applyChangesFromScrapeSourceModal() {
    const {sourceBuilder, request, response} = this.scrapeSourceModalContext;
    sourceBuilder.request = request;
    sourceBuilder.response = response;
    await this.dismissScrapeSourceModal()
  }

  // -- agent modal ------------------------------------------------------------

  async openAgentModal() {
    await this.agentModalElement.present()
  }

  dismissAgentModal() {
    return this.agentModalElement.dismiss()
  }

  applyChangesFromAgentModal() {
    return this.agentModalElement.dismiss()
  }

  // -- segmented modal --------------------------------------------------------

  async openSegmentedDeliveryModal() {
    this.segmentedDeliveryModalContext = { segmented: cloneDeep(this.builder.sink.segmented) };
    await this.segmentedDeliveryModalElement.present()
  }

  dismissSegmentedDeliveryModal() {
    this.segmentedDeliveryModalContext = null;
    return this.segmentedDeliveryModalElement.dismiss()
  }
  applyChangesFromSegmentedDeliveryModal() {
    this.builder.sink.segmented = this.segmentedDeliveryModalContext.segmented;
    return this.dismissSegmentedDeliveryModal()
  }

  // openResourceMapperModal(source: SourceBuilder, responseMapper: ResponseMapper = null) {
  //   switch (responseMapper || source.getResponseMapperType()) {
  //     case 'feed':
  //       return this.openWebsiteToFeedModal(source);
  //     case 'fragment':
  //       return this.openScrapeSourceModal(source)
  //   }
  // }

  // ---------------------------------------------------------------------------

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

  labelAgent(agent: Agent) {
    return `${agent.version} - ${agent.osInfo}`;
  }

  getLabelForSource(source: SourceBuilder) {
    return source.produces()
      .filter(it => it)
      .map(it => it.label).join(', ') || '...'
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

  private getDefaultForSinkTarget(target: SinkTarget): SinkTargetSpec {
    switch (target) {
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

  async handleSinkScopeChange(scope: SinkScope, spec: SinkSpec) {
    switch (scope) {
      case 'segmented':
        spec.segmented = {}
        await this.openSegmentedDeliveryModal()
        break;
      case 'unscoped':
        spec.segmented = null;
        break;
      default:
        throw new Error('not supported')
    }
  }

  async save() {
    const spec = this.builder.build();
    await this.importerService.createImporters({
      feeds: [],
      bucket: {},
      protoImporter: {}
    })
  }

  hasError(source: SourceBuilder): boolean {
    // !source.pending && !source.error && (i === 0 || builder.sources.needsResourceMapper(source))
    return (!source.pending && !isDefined(source.response)) || this.builder.sources.needsResourceMapper(source);
  }
}
