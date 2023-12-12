import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { GqlBucketCreateInput, GqlBucketWhereInput, GqlScrapeRequestInput, GqlVisibility } from '../../../generated/graphql';
import { omit } from 'lodash-es';
import { Agent, AgentService } from '../../services/agent.service';
import { Field, isDefined } from './scrape-builder';
import { ScrapeService } from '../../services/scrape.service';
import { KeyLabelOption } from '../../components/select/select.component';
import { ModalController } from '@ionic/angular';
import { ImporterService } from '../../services/importer.service';
import {
  ScrapeSourceComponent,
  ScrapeSourceComponentProps,
  ScrapeSourceDismissalData,
  TypedFormGroup
} from '../../components/scrape-source/scrape-source.component';
import { FormArray, FormControl, FormGroup, Validators, ɵFormGroupValue, ɵTypedOrUntyped } from '@angular/forms';
import { BucketsModalComponent, BucketsModalComponentProps } from '../buckets-modal/buckets-modal.component';
import { Subscription } from 'rxjs';
import { ModalService } from '../../services/modal.service';
import { BasicBucket, ScrapeResponse } from '../../graphql/types';
import { BucketService } from '../../services/bucket.service';

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

// type SinkScope = 'segmented' | 'unscoped'

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

type RefineByFieldCreation = {
  field?: ScrapeField
  regex?: string
  aliasAs?: string
}
type RefineByFieldUpdate = {
  field?: ScrapeField
  regex?: string
  replacement?: string
}

type RefinePolicy = {
  create?: RefineByFieldCreation,
  update?: RefineByFieldUpdate}

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

type FieldFilterType = 'include' | 'exclude'

type FieldFilterOperator = 'contains' | 'endsWith' | 'startsWith'
type FieldFilter = {
  type: FieldFilterType
  field: string
  negate: boolean
  operator: FieldFilterOperator
  value: string
}

export type SegmentedOutput = {
  filter?: string,
  orderBy?: string,
  orderAsc?: boolean,
  size?: number,
  digest?: boolean
  scheduled?: ScheduledPolicy
}

type SinkTargetWrapper = {
  type: SinkTargetType,
  oneOf: SinkTarget
}

type EmailSink = {
  address: string
};
type WebhookSink = {
  url: string
};
type BucketSinkType = 'existing' | 'create';
type BucketSink = {
  type: BucketSinkType
  oneOf: {
    existing?: GqlBucketWhereInput
    create?: GqlBucketCreateInput
  }
};
type SinkTarget = {
  email?: EmailSink,
  webhook?: WebhookSink,
  bucket?: BucketSink
}

type Sink = {
  isSegmented: boolean,
  segmented?: SegmentedOutput,
  targets: SinkTargetWrapper[]
}

export type Source = {
  // output?: ScrapeField | ScrapeField[]
  request: GqlScrapeRequestInput;
  response?: ScrapeResponse;
}

export type FeedBuilder = {
  source: Source[]
  agent?: Agent;
  refine: RefinePolicy[];
  fetch: ScheduledPolicy;
  filters: FieldFilter[]
  sink: Sink
}

export interface FeedBuilderModalComponentProps {
  feedBuilder: Partial<FeedBuilder>;
}

interface SegmentedDeliveryModalContext {
  segmented: SegmentedOutput;
}

const EVERY_FOUR_HOURS = '0 */4 * * *';

@Component({
  selector: 'app-feed-builder',
  templateUrl: './feed-builder-modal.component.html',
  styleUrls: ['./feed-builder-modal.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FeedBuilderModalComponent implements OnInit, OnDestroy, FeedBuilderModalComponentProps {

  @Input({ required: true })
  feedBuilder: FeedBuilder;

  @ViewChild('segmentedDeliveryModal')
  segmentedDeliveryModalElement: HTMLIonModalElement;

  feedBuilderFg = new FormGroup({
    source: new FormArray<FormControl<Source>>([], {validators: [Validators.required, Validators.minLength(1)]}),
    refine: new FormArray<FormGroup<TypedFormGroup<RefinePolicy>>>([]),
    fetch: new FormGroup<TypedFormGroup<FetchPolicy>>({
      cronString: new FormControl<string>(''),
      plugins: new FormArray<FormGroup<TypedFormGroup<PluginRef>>>([])
    }),
    agent: new FormControl<Agent>(null, {nonNullable: false, validators: [Validators.required]}),
    filters: new FormArray<FormGroup<TypedFormGroup<FieldFilter>>>([]),
    sink: new FormGroup<TypedFormGroup<Sink>>({
      targets: new FormArray<FormGroup<TypedFormGroup<SinkTargetWrapper>>>([]),
      isSegmented: new FormControl<boolean>(false),
      segmented: new FormGroup<TypedFormGroup<SegmentedOutput>>({
        digest: new FormControl<SegmentedOutput["digest"] | null>(false, {validators: [Validators.required]}),
        filter: new FormControl<SegmentedOutput["filter"] | null>(''),
        orderBy: new FormControl<SegmentedOutput["orderBy"] | null>('', {validators: [Validators.required, Validators.minLength(1)]}),
        orderAsc: new FormControl<SegmentedOutput["orderAsc"] | null>(false),
        scheduled: new FormGroup<TypedFormGroup<ScheduledPolicy>>({
          cronString: new FormControl<ScheduledPolicy["cronString"] | null>('', {validators: [Validators.required, Validators.minLength(1), Validators.maxLength(5)]})
        }, {validators: [Validators.required]}),
        size: new FormControl<SegmentedOutput["size"] | null>(10, {validators: [Validators.required, Validators.min(10), Validators.max(100)]})
      }),
    }, {validators: [Validators.required, Validators.minLength(1)]}),
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
      key: 'webhook',
      label: 'Webhook'
    },
    {
      key: 'bucket',
      label: 'Bucket'
    }
  ];
  fetchFrequencyOptions = this.getFetchFrequencyOptions();

  timeSegments: KeyLabelOption<number>[] = this.getTimeSegmentsOptions();
  fields: Field[];
  hasFields: boolean;
  fetchFrequencyFC: FormControl<string>;

  private subscriptions: Subscription[] = [];
  protected readonly CUSTOM_FETCH_FREQUENCY = 'custom';

  constructor(private readonly scrapeService: ScrapeService,
              private readonly changeRef: ChangeDetectorRef,
              private readonly modalService: ModalService,
              private readonly bucketService: BucketService,
              private readonly importerService: ImporterService,
              private readonly modalCtrl: ModalController,
              private readonly agentService: AgentService) {
  }

  async ngOnInit(): Promise<void> {
    this.feedBuilderFg.patchValue(this.feedBuilder);
    this.fetchFrequencyFC = new FormControl<string>('', {nonNullable: false})

    this.subscriptions.push(
      this.fetchFrequencyFC.valueChanges.subscribe(cronString => {
        if (cronString !== this.CUSTOM_FETCH_FREQUENCY) {
          this.feedBuilderFg.controls.fetch.controls.cronString.patchValue(cronString);
        }
      }),
      this.feedBuilderFg.controls.fetch.controls.cronString.valueChanges.subscribe(customCronString => {
        const match = this.getFetchFrequencyOptions()
          .map(option => option.key)
          .find(cronString => cronString === customCronString.trim());

        const value = match ?? this.CUSTOM_FETCH_FREQUENCY;
        if (this.fetchFrequencyFC.value !== value) {
          this.fetchFrequencyFC.setValue(value)
        }
      })
    );
    this.fetchFrequencyFC.setValue(EVERY_FOUR_HOURS);

    // this.builder.valueChanges
    //   .pipe(debounce(() => interval(50)))
    //   .subscribe(() => {
    //
    //     this.fields = this.builder.produces().flatMap(a => a.fields);
    //     this.hasFields = this.fields.length == 0;
    //     this.changeRef.detectChanges();
    //   });

    this.agents = await this.agentService.getAgents();

    this.parse(this.feedBuilder);
    this.changeRef.detectChanges();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  getRequestLabel(source: FormControl<Source>) {
    return source.value.request?.page?.url?.replace(/http[s]?:\/\//, '');
  }

  getResponseLabel(sourceFc: FormControl<Source>) {
    let engine;
    const source = sourceFc.value;
    if (!source) {
      return 'specify';
    }
    if (source?.request?.page?.prerender) {
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

    return `${engine} ${responseType}`;
  }

  // -- scrape modal -----------------------------------------------------------

  async openScrapeSourceModal(sourceFg: FormControl<Source | null> = null) {
    const componentProps: ScrapeSourceComponentProps = {
      source: sourceFg?.value
    };

    const modal = await this.modalCtrl.create({
      component: ScrapeSourceComponent,
      componentProps,
      backdropDismiss: false
    });

    await modal.present();
    const response = await modal.onDidDismiss<ScrapeSourceDismissalData>();
    if (response.data) {
      const source: Source = {
        request: response.data.request,
        response: response.data.response,
      };
      if (sourceFg) {
        sourceFg.setValue(source);
      } else {
        await this.addSource(source);
      }
      this.changeRef.detectChanges();
    }
  }

  // -- agent modal ------------------------------------------------------------

  async openAgentModal() {
    const agent = await this.modalService.openAgentModal({});
    this.feedBuilderFg.controls.agent.setValue(agent);
  }

  // -- segmented modal --------------------------------------------------------

  async openSegmentedDeliveryModal() {
    this.segmentedDeliveryModalContext = { segmented: {} };
    await this.segmentedDeliveryModalElement.present();
  }

  dismissSegmentedDeliveryModal() {
    this.segmentedDeliveryModalContext = null;
    return this.segmentedDeliveryModalElement.dismiss();
  }

  applyChangesFromSegmentedDeliveryModal() {
    // this.builder.sink.segmented = this.segmentedDeliveryModalContext.segmented;
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


  /**
   *   *    *    *    *    *
   *   ┬    ┬    ┬    ┬    ┬
   *   │    │    │    │    |
   *   │    │    │    │    └ day of week (0 - 7, 1L - 7L) (0 or 7 is Sun)
   *   │    │    │    └───── month (1 - 12)
   *   │    │    └────────── day of month (1 - 31, L)
   *   │    └─────────────── hour (0 - 23)
   *   └──────────────────── minute (0 - 59)
   * @private
   */
  private getFetchFrequencyOptions(): KeyLabelOption<string>[] {
    return [
      {
        key: '0 * * * *',
        label: 'Every hour'
      },
      {
        key: '0 */2 * * *',
        label: 'Every 2 hours'
      },
      {
        key: EVERY_FOUR_HOURS,
        label: 'Every 4 hours',
      },
      {
        key: '0 */8 * * *',
        label: 'Every 8 hours'
      },
      {
        key: '0 */12 * * *',
        label: 'Every 12 hours'
      },
      {
        key: '0 0 * * *',
        label: 'Every day'
      },
      {
        key: '',
        label: 'Every 2 days'
      },
      {
        key: '0 0 * * 0',
        label: 'Every week'
      },
      {
        key: '0 0 1 * *',
        label: 'Every month'
      },
      {
        key: 'custom',
        label: 'Custom'
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

  getLabelForAgent() {
    if (this.feedBuilderFg.controls.agent.valid) {
      const agent = this.feedBuilderFg.value.agent;
      return `${agent.version} - ${agent.osInfo}`;
    } else {
      return 'Agent...'
    }
  }

  addFieldRefinement(option: KeyLabelOption<RefineType>) {
    switch (option.key) {
      case 'create':
        return this.feedBuilderFg.controls.refine.push(new FormGroup<TypedFormGroup<RefinePolicy>>({
            create: new FormGroup<TypedFormGroup<RefinePolicy["create"]>>({})
          })
        );
      case 'update':
        return this.feedBuilderFg.controls.refine.push(new FormGroup<TypedFormGroup<RefinePolicy>>({
            update: new FormGroup<TypedFormGroup<RefinePolicy["update"]>>({})
          })
        );
      default:
        throw new Error('not supported');
    }
  }

  closeModal() {
    return this.modalCtrl.dismiss();
  }

  // addTargetToSink(target: SinkTargetType, sink: SinkSpec) {
  //   // sink.targets.push(this.getDefaultForSinkTarget(target));
  // }

  // async handleSinkScopeChange(scope: SinkScope, spec: SinkSpec) {
  //   switch (scope) {
  //     case 'segmented':
  //       spec.segmented = {};
  //       await this.openSegmentedDeliveryModal();
  //       break;
  //     case 'unscoped':
  //       spec.segmented = null;
  //       break;
  //     default:
  //       throw new Error('not supported');
  //   }
  // }

  async save() {
    // const spec = this.builder.build();
    const req = {
      importers: [
        {
          source: {},
          sink: {}
        }
      ]
    }
    await this.importerService.createImporters({
      feeds: [
        {
          create: {

          }
        }
      ],
      bucket: { },
      protoImporter: {}
    })
  }

  private async saveWithSources() {

  }

  getOutput(source: FormControl<Source>): string {
    // if (isArray(source.value.output)) {
    //   return (source.value.output as Field[]).join(', ')
    // } else {
    //   return source.value?.output?.name ?? '...';
    // }
    if (source.value.response) {
      return 'foo';
    } else {
      return '...';
    }
  }

  isProvidesAsciiFields(): boolean {
    return true;
  }
  needsAgent(): boolean {
    return this.feedBuilderFg.value.source.some(source => isDefined(source.request.page.prerender))
  }

  addFilter(data: FieldFilter = null): void {
    const filter = new FormGroup<TypedFormGroup<FieldFilter>>({
      value: new FormControl<FieldFilter["value"] | null>('', {nonNullable: true, validators: [Validators.required, Validators.minLength(3)]}),
      field: new FormControl<string>(null, {nonNullable: true, validators: [Validators.required]}),
      negate: new FormControl<boolean>(false, {nonNullable: true, validators: [Validators.required]}),
      type: new FormControl<FieldFilterType>('include', {nonNullable: true, validators: [Validators.required]}),
      operator: new FormControl<FieldFilterOperator>('contains', {nonNullable: true, validators: [Validators.required]})
    });

    if (data) {
      filter.setValue(data);
    } else {
      filter.setValue({
        field: null,
        value: '',
        negate: false,
        type: 'include',
        operator: 'contains'
      });
    }

    this.feedBuilderFg.controls.filters.push(filter);
  }

  getAvailableFields(): KeyLabelOption<string>[] {
    return [
      {
        key: 'title',
        label: 'Title'
      },
      {
        key: 'description',
        label: 'Description'
      },
      {
        key: 'link',
        label: 'Link'
      }
    ];
  }

  getFiltersFg(): FormGroup<TypedFormGroup<FieldFilter>>[] {
    const filtersFg: FormGroup<TypedFormGroup<FieldFilter>>[] = [];
    for(let i=0; i<this.feedBuilderFg.controls.filters.length; i++) {
      filtersFg.push(this.feedBuilderFg.controls.filters.at(i));
    }
    return filtersFg;
  }

  getFieldFilterTypes(): KeyLabelOption<FieldFilterType>[] {
    return [
      {
        key: 'include',
        label: 'include'
      },
      {
        key: 'exclude',
        label: 'exclude'
      },
    ];
  }

  getFieldFilterOperators(): KeyLabelOption<FieldFilterOperator>[] {
    return [
      {
        key: 'contains',
        label: 'contains'
      },
      {
        key: 'startsWith',
        label: 'startsWith'
      },
      {
        key: 'endsWith',
        label: 'endsWith'
      },
    ];
  }

  getSourcesFc(): FormControl<Source>[] {
    const filtersFg: FormControl<Source>[] = [];
    for(let i=0; i<this.feedBuilderFg.controls.source.length; i++) {
      filtersFg.push(this.feedBuilderFg.controls.source.at(i) as FormControl<Source>);
    }
    return filtersFg;

  }

  getTargets(sinkFg: FormGroup<TypedFormGroup<Sink>>): FormGroup<TypedFormGroup<SinkTargetWrapper>>[] {
    const targetFg: FormGroup<TypedFormGroup<SinkTargetWrapper>>[] = [];
    for(let i=0; i<sinkFg.controls.targets.length; i++) {
      targetFg.push(sinkFg.controls.targets.at(i));
    }
    return targetFg;
  }

  async addTarget(sinkTargetType: SinkTargetType, data: SinkTargetWrapper = null) {
    const sinkTarget = new FormGroup<TypedFormGroup<SinkTargetWrapper>>({
      type: new FormControl<SinkTargetType>(sinkTargetType),
      oneOf: new FormGroup<TypedFormGroup<SinkTarget>>({
        bucket: new FormGroup<TypedFormGroup<BucketSink>>({
          type: new FormControl<BucketSinkType>(null),
          oneOf: new FormGroup<TypedFormGroup<BucketSink["oneOf"]>>({
            create: new FormControl<GqlBucketCreateInput>(null) as any,
            existing: new FormControl<GqlBucketWhereInput>(null) as any,
          })
        }),
        email: new FormGroup<TypedFormGroup<EmailSink>>({
          address: new FormControl<EmailSink["address"] | null>('', {nonNullable: true, validators: [Validators.required, Validators.email]})
        }),
        webhook: new FormGroup<TypedFormGroup<WebhookSink>>({
          url: new FormControl<WebhookSink["url"] | null>('', {nonNullable: true, validators: [Validators.required, Validators.minLength(1)]})
        })
      })
    });

    sinkTarget.controls.type.valueChanges.subscribe(type => {
      Object.keys(sinkTarget.controls.oneOf.controls)
        .forEach(otherKey => {
          if (otherKey === type) {
            sinkTarget.controls.oneOf.controls[otherKey].enable();
          } else {
            sinkTarget.controls.oneOf.controls[otherKey].disable();
          }
        });
    });

    if (data) {
      sinkTarget.patchValue(data);
      this.feedBuilderFg.controls.sink.controls.targets.push(sinkTarget);
    } else {
      switch (sinkTargetType) {
        case 'bucket':
          await this.openBucketsModal(sinkTarget)
            .then(() => {
              this.feedBuilderFg.controls.sink.controls.targets.push(sinkTarget)
            });
          break;
        default:
          this.feedBuilderFg.controls.sink.controls.targets.push(sinkTarget)
          break;
      }
    }
  }

  async openBucketsModal(target: FormGroup<TypedFormGroup<SinkTargetWrapper>>): Promise<void> {
    const componentProps: BucketsModalComponentProps = {
      // scrapeRequest: sourceFg?.value?.request
      onClickBucket: async (bucket: BasicBucket) => {
        await this.modalCtrl.dismiss(bucket);
      }
    };

    const modal = await this.modalCtrl.create({
      component: BucketsModalComponent,
      componentProps,
      backdropDismiss: false
    });

    await modal.present();
    const response = await modal.onDidDismiss();
    console.log('openBucketsModal', response.data);
    if (response.data) {
      target.patchValue({
        type: 'bucket',
        oneOf: {
          bucket: response.data
        }
      });
      this.changeRef.markForCheck();
    }
  }

  getLabelForBucket(sink: ɵTypedOrUntyped<TypedFormGroup<SinkTarget['bucket']>, ɵFormGroupValue<TypedFormGroup<SinkTarget['bucket']>>, any>): string {
    console.log('getLabelForBucket', sink)
    if (sink?.oneOf?.existing) {
      return `existing #${sink?.oneOf?.existing?.where?.id}`;
    } else {
      return `new #${sink?.oneOf?.create?.title}`;
    }
  }

  async openCodeEditor() {
    const inData = omit(this.feedBuilderFg.value, ['source.response']);
    const data: FeedBuilder = await this.modalService.openCodeEditorModal(JSON.stringify(inData, null, 2));
    this.parse(data);
  }

  private parse(data: FeedBuilder) {
    if (data) {
      data.source?.forEach(source => this.addSource(source))
      this.feedBuilderFg.controls.agent.patchValue(data.agent);
      this.feedBuilderFg.controls.fetch.patchValue(data.fetch);
      this.feedBuilderFg.controls.sink.patchValue(data.sink);
      this.feedBuilderFg.controls.refine.patchValue(data.refine);
      data.sink.targets.map(target => this.addTarget(target.type, target))
      data.filters?.forEach(filter => this.addFilter(filter));
      this.changeRef.detectChanges();
    }
  }

  private async addSource(source: Source) {
    const sourceFg = new FormControl<Source>(null);
    if (source) {
      sourceFg.setValue(source);
      if (!source.response) {
        this.scrapeService.scrape(source.request)
          .then((response) => {
            source.response = response;
            this.changeRef.detectChanges();
          })
      }
    }
    this.feedBuilderFg.controls.source.push(sourceFg)
  }
}
