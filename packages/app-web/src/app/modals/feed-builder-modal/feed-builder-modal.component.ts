import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit, ViewChild } from '@angular/core';
import { GqlBucketCreateInput, GqlBucketWhereInput, GqlScrapeEmitType, GqlScrapeRequestInput } from '../../../generated/graphql';
import { cloneDeep, isNull, isUndefined } from 'lodash-es';
import { Agent, AgentService } from '../../services/agent.service';
import { Field, ScrapeBuilder, ScrapeBuilderSpec, SegmentedOutputSpec, SinkSpec, SinkTargetSpec, SourceBuilder } from './scrape-builder';
import { ScrapeService } from '../../services/scrape.service';
import { debounce, interval } from 'rxjs';
import { KeyLabelOption } from '../../components/select/select.component';
import { ModalController } from '@ionic/angular';
import { ImporterService } from '../../services/importer.service';
import { ScrapeSourceComponent, ScrapeSourceComponentProps, TypedFormGroup } from '../../components/scrape-source/scrape-source.component';
import { FormArray, FormControl, FormGroup, Validators } from '@angular/forms';

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


type RefineType = 'create' | 'update'

type SinkTargetType = 'bucket' | 'email' | 'webhook'

type FeedType = 'existing' | 'new'

type SinkScope = 'segmented' | 'unscoped'

type ScrapeFieldType = 'text' | 'markup' | 'base64' | 'url' | 'date' | 'number'

type ScrapeField = {
  type: ScrapeFieldType
  name: string
}

const Feed: ScrapeField[] = [
  {
    name: 'title',
    type: 'text'
  },
  {
    name: 'description',
    type: 'text'
  },
  {
    name: 'link',
    type: 'url'
  },
  {
    name: 'createdAt',
    type: 'date'
  }
];

type RefinePolicy = {
  create?: {
    field?: ScrapeField
    regex?: string
    aliasAs?: string
  },
  update?: {
    field?: ScrapeField
    regex?: string
    replacement?: string
  }
}

type ScheduledPolicy = {
  cronString: string
}

type PluginRef = {
  id: string
  data: object
}

type FetchPolicy = {
  plugins?: PluginRef[]
} & ScheduledPolicy

type FieldFilter = {
  type: 'include' | 'exclude'
  field: ScrapeField
  negate: boolean
  operator: 'contains' | 'endsWith' | 'startsWith'
  value: string
}

type SegmentedOutput = {
  filter?: {
    createdAt: {
      gt: {
        value: string
      }
    }
  },
  orderBy?: ScrapeField,
  orderAsc?: boolean,
  size?: number,
  digest?: boolean
  scheduled?: ScheduledPolicy
}

type SinkTarget = {
  email?: {
    address: string
  },
  webhook?: {
    url: string
  },
  feed?: {
    existing?: GqlBucketWhereInput
    create?: GqlBucketCreateInput
  }
}

type Sink = {
  segmented?: SegmentedOutput,
  targets: SinkTarget[]
}

type Source = {
  output: ScrapeField | ScrapeField[]
  request: GqlScrapeRequestInput;
}

type FeedBuilder = {
  source: Source[]
  agent?: Agent;
  refine: RefinePolicy[];
  fetch: ScheduledPolicy;
  filters: FieldFilter[]
  sinks: Sink[]
}

export interface FeedBuilderCardComponentProps {
  scrapeBuilderSpec: ScrapeBuilderSpec;
}

interface SegmentedDeliveryModalContext {
  segmented: SegmentedOutputSpec;
}

@Component({
  selector: 'app-feed-builder',
  templateUrl: './feed-builder-modal.component.html',
  styleUrls: ['./feed-builder-modal.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FeedBuilderModalComponent implements OnInit, FeedBuilderCardComponentProps {

  @Input({ required: true })
  scrapeBuilderSpec: ScrapeBuilderSpec;

  @ViewChild('segmentedDeliveryModal')
  segmentedDeliveryModalElement: HTMLIonModalElement;

  @ViewChild('agentModal')
  agentModalElement: HTMLIonModalElement;

  builder: ScrapeBuilder;
  // builderFg = new FormGroup({
  //   source: new FormGroup({
  //     output: new FormControl<string>(null),
  //     sources: new FormArray<TypedFormGroup<FeedSource>>([
  //       new FormGroup({
  //         request: new FormControl<GqlScrapeRequestInput>(null),
  //         output: new FormControl<string>(null)
  //       })
  //     ])
  //   }),
  //   agent: new FormControl<Agent>(null),
  //   filters: new FormArray([]),
  //   fetch: new FormGroup({
  //     interval: new FormControl<number>(null),
  //     startingAt: new FormControl<string>(null),
  //     plugins: new FormArray([])
  //   }),
  //   refines: new FormArray([]),
  //   persist: new FormGroup({
  //     scope: new FormGroup({
  //       type: new FormControl<SinkScope>(null),
  //       scoped: new FormGroup({})
  //     }),
  //     sinks: new FormArray([])
  //   })
  // });

  feedBuilderFg: FormGroup<TypedFormGroup<FeedBuilder>> = new FormGroup({
    source: new FormArray<FormGroup<TypedFormGroup<Source>>>([], {validators: [Validators.required, Validators.minLength(1)]}),
    refine: new FormArray<FormGroup<TypedFormGroup<RefinePolicy>>>([]),
    fetch: new FormGroup<TypedFormGroup<FetchPolicy>>({
      cronString: new FormControl<string>(''),
      plugins: new FormArray<FormGroup<TypedFormGroup<PluginRef>>>([])
    }),
    filters: new FormArray<FormGroup<TypedFormGroup<FieldFilter>>>([]),
    sinks: new FormArray<FormGroup<TypedFormGroup<Sink>>>([], {validators: [Validators.required, Validators.minLength(1)]}),
  });

  agents: Agent[] = [];

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
  sinkTargetOptions: KeyLabelOption<SinkTargetType>[] = [
    {
      key: 'email',
      label: 'Email'
    },
    {
      key: 'bucket',
      label: 'Bucket'
    },
    {
      key: 'webhook',
      label: 'Webhook'
    }
  ];
  fetchFrequencyOptions = this.getFetchFrequencyOptions();
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
  ];
  timeSegments: KeyLabelOption<number>[] = this.getTimeSegmentsOptions();
  fields: Field[];
  hasFields: boolean;

  constructor(private readonly scrapeService: ScrapeService,
              private readonly changeRef: ChangeDetectorRef,
              private readonly importerService: ImporterService,
              private readonly modalCtrl: ModalController,
              private readonly agentService: AgentService) {
  }

  async ngOnInit(): Promise<void> {
    this.builder = new ScrapeBuilder(this.scrapeBuilderSpec);
    this.builder.valueChanges
      .pipe(debounce(() => interval(50)))
      .subscribe(() => {

        this.fields = this.builder.produces().flatMap(a => a.fields);
        this.hasFields = this.fields.length == 0;
        this.changeRef.detectChanges();
      });

    this.agents = await this.agentService.getAgents();
    this.feedBuilderFg.patchValue({
      fetch: {
        cronString: ''
      }
    });
  }

  addSource() {
    const sourceBuilder = this.builder.sources.add();
    return this.openScrapeSourceModal(sourceBuilder);
  }

  getRequestLabel(source: SourceBuilder) {
    return source.request?.page?.url?.replace(/http[s]?:\/\//, '');
  }

  getResponseLabel(source: SourceBuilder) {
    let engine;
    if (source.request?.page?.prerender) {
      const actionsCount = source.request?.page?.actions?.length || 0;
      engine = `chrome, ${actionsCount} actions`;
    } else {
      engine = 'static';
    }

    let responseType: string;
    if (source.response) {
      responseType = `${source.response.debug.contentType.replace(/;.*/, '')}`;
    } else {
      responseType = '...';
    }

    return `using ${engine} ${responseType}`;
  }

  // -- scrape modal -----------------------------------------------------------

  async openScrapeSourceModal(sourceBuilder: SourceBuilder) {
    const componentProps: ScrapeSourceComponentProps = {
      scrapeRequest: sourceBuilder.request
    };

    const modal = await this.modalCtrl.create({
      component: ScrapeSourceComponent,
      componentProps,
      backdropDismiss: false
    });

    await modal.present();
    const response = await modal.onDidDismiss();
    if (response.data) {
      sourceBuilder.request = response.data.request;
      sourceBuilder.response = response.data.response;
      this.changeRef.detectChanges();
    }
  }

  // -- agent modal ------------------------------------------------------------

  async openAgentModal() {
    await this.agentModalElement.present();
  }

  dismissAgentModal() {
    return this.agentModalElement.dismiss();
  }

  applyChangesFromAgentModal() {
    return this.agentModalElement.dismiss();
  }

  // -- segmented modal --------------------------------------------------------

  async openSegmentedDeliveryModal() {
    this.segmentedDeliveryModalContext = { segmented: cloneDeep(this.builder.sink.segmented) };
    await this.segmentedDeliveryModalElement.present();
  }

  dismissSegmentedDeliveryModal() {
    this.segmentedDeliveryModalContext = null;
    return this.segmentedDeliveryModalElement.dismiss();
  }

  applyChangesFromSegmentedDeliveryModal() {
    this.builder.sink.segmented = this.segmentedDeliveryModalContext.segmented;
    return this.dismissSegmentedDeliveryModal();
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
        label: 'Last month'
      }
    ];
  }

  labelAgent(agent: Agent) {
    return `${agent.version} - ${agent.osInfo}`;
  }

  addFieldRefinement(option: KeyLabelOption<RefineType>) {
    switch (option.key) {
      case 'create':
        return this.builder.addRefine({
          create: {}
        });
      case 'update':
        return this.builder.addRefine({
          update: {}
        });
      default:
        throw new Error('not supported');
    }
  }

  isDefined(v: any | undefined): boolean {
    return !isNull(v) && !isUndefined(v);
  }

  private getDefaultForSinkTarget(target: SinkTargetType): SinkTargetSpec {
    switch (target) {
      case 'webhook':
        return {
          webhook: {
            url: ''
          }
        };
      case 'email':
        return {
          email: {
            address: ''
          }
        };
      default:
        throw new Error('not supported');
    }
  }

  closeModal() {
    return this.modalCtrl.dismiss();
  }

  handleFeedType(type: FeedType, sink: SinkTargetSpec) {
    switch (type) {
      case 'new':
        sink.feed.create = {};
        break;
      case 'existing':
        sink.feed.existing = {};
        break;
      default:
        throw new Error('not supported');
    }
  }

  addTargetToSink(target: SinkTarget, sink: SinkSpec) {
    // sink.targets.push(this.getDefaultForSinkTarget(target));
  }

  async handleSinkScopeChange(scope: SinkScope, spec: SinkSpec) {
    switch (scope) {
      case 'segmented':
        spec.segmented = {};
        await this.openSegmentedDeliveryModal();
        break;
      case 'unscoped':
        spec.segmented = null;
        break;
      default:
        throw new Error('not supported');
    }
  }

  async save() {
    // const spec = this.builder.build();
    // await this.importerService.createImporters({
    //   feeds: [],
    //   bucket: {},
    //   protoImporter: {}
    // })
  }

  getEmitType(source: SourceBuilder): string {
    if (source.request.emit?.length > 0) {
      console.log(source.request.emit);
      const emit = source.request.emit[0];
      return emit.types.join(', ');
    } else {
      if (source.request.debug) {
        if (source.request.debug.html) {
          return GqlScrapeEmitType.Markup;
        } else {
          if (source.request.debug.screenshot) {
            return GqlScrapeEmitType.Pixel;
          }
        }
      }
    }
    return '...';
  }
}
