import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import {
  GqlPluginExecutionInput,
  GqlPluginType,
  GqlRetentionInput,
  GqlScrapeRequestInput,
  GqlSegmentInput,
  GqlVisibility
} from '../../../generated/graphql';
import { cloneDeep, omit, uniq, unset } from 'lodash-es';
import { Agent, AgentService } from '../../services/agent.service';
import { Field, isDefined } from './scrape-builder';
import { ScrapeService } from '../../services/scrape.service';
import { ModalController } from '@ionic/angular';
import {
  ScrapeSourceComponent,
  ScrapeSourceComponentProps,
  ScrapeSourceDismissalData,
  TypedFormGroup
} from '../../components/scrape-source/scrape-source.component';
import { FormArray, FormControl, FormGroup, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';
import { ModalService } from '../../services/modal.service';
import { ScrapeResponse } from '../../graphql/types';
import { SourceSubscriptionService } from '../../services/source-subscription.service';
import { KeyLabelOption } from '../../elements/select/select.component';
import { PluginService } from '../../services/plugin.service';

/**
 * IDEEN
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

export type DeepPartial<T> = T extends object
  ? {
    [P in keyof T]?: DeepPartial<T[P]>;
  }
  : T;

type SinkTargetType = 'email' | 'webhook';

type ScheduledPolicy = {
  cronString: string;
};

type PluginRef = {
  id: string;
  data: object;
};

type FetchPolicy = {
  plugins?: PluginRef[];
} & ScheduledPolicy;

type FieldFilterType = 'include' | 'exclude';

type FieldFilterOperator = 'matches' | 'contains' | 'endsWith' | 'startsWith';
type FieldFilter = {
  type: FieldFilterType;
  field: string;
  negate: boolean;
  operator: FieldFilterOperator;
  value: string;
};

export type SegmentedOutput = {
  filter?: string;
  orderBy?: string;
  orderAsc?: boolean;
  size?: number;
  digest?: boolean;
  scheduled?: ScheduledPolicy;
};

type SinkTargetWrapper = {
  type: SinkTargetType;
  oneOf: SinkTarget;
};

type EmailSink = {
  address: string;
};
type WebhookSink = {
  url: string;
};
type SinkTarget = {
  email?: EmailSink;
  webhook?: WebhookSink;
};

type Sink = {
  isSegmented: boolean;
  segmented?: SegmentedOutput;
  targets: SinkTargetWrapper[];
  hasRetention: boolean;
  retention: GqlRetentionInput;
  visibility: GqlVisibility;
  title: string;
  description: string;
};

export type Source = {
  // output?: ScrapeField | ScrapeField[]
  request: GqlScrapeRequestInput;
  response?: ScrapeResponse;
};

export type FeedBuilder = {
  sources: Source[];
  agent?: Agent;
  fetch: ScheduledPolicy;
  filters: FieldFilter[];
  sink: Sink;
};

export interface FeedBuilderModalComponentProps {
  feedBuilder: DeepPartial<FeedBuilder>;
}

export type FeedBuilderModalData = FeedBuilder;

interface SegmentedDeliveryModalContext {
  segmented: SegmentedOutput;
}

const EVERY_FOUR_HOURS = '0 0 */4 * * *';

export enum FeedBuilderModalComponentExitRole {
  dismiss = 'dismiss',
  login = 'login',
}

@Component({
  selector: 'app-feed-builder-modal',
  templateUrl: './feed-builder-modal.component.html',
  styleUrls: ['./feed-builder-modal.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FeedBuilderModalComponent
  implements OnInit, OnDestroy, FeedBuilderModalComponentProps {
  feedBuilder: FeedBuilder;

  @ViewChild('segmentedDeliveryModal')
  segmentedDeliveryModalElement: HTMLIonModalElement;

  feedBuilderFg = new FormGroup({
    source: new FormArray<FormControl<Source>>([], {
      validators: [Validators.required, Validators.minLength(1)]
    }),
    fetch: new FormGroup<TypedFormGroup<FetchPolicy>>({
      cronString: new FormControl<string>('', {
        nonNullable: true,
        validators: Validators.pattern('([^ ]+ ){5}[^ ]+')
      }),
      plugins: new FormArray<FormGroup<TypedFormGroup<PluginRef>>>([])
    }),
    agent: new FormControl<Agent>(null, { nonNullable: false, validators: [] }),
    filters: new FormArray<FormGroup<TypedFormGroup<FieldFilter>>>([], {
      validators: [Validators.max(3)]
    }),
    sink: new FormGroup<TypedFormGroup<Sink>>(
      {
        targets: new FormArray<FormGroup<TypedFormGroup<SinkTargetWrapper>>>(
          []
        ),
        hasRetention: new FormControl<boolean>(false, {
          validators: [Validators.required]
        }),
        visibility: new FormControl<GqlVisibility>(GqlVisibility.IsPrivate, {
          validators: [Validators.required]
        }),
        title: new FormControl<string>('', {
          validators: [Validators.required, Validators.minLength(3)]
        }),
        description: new FormControl<string>(''),
        retention: new FormGroup<TypedFormGroup<GqlRetentionInput>>({
          maxItems: new FormControl<number>(null, {
            validators: [Validators.min(2)]
          }),
          maxAgeDays: new FormControl<number>(null, {
            validators: [Validators.min(2)]
          })
        }),
        isSegmented: new FormControl<boolean>(false),
        segmented: new FormGroup<TypedFormGroup<SegmentedOutput>>({
          digest: new FormControl<SegmentedOutput['digest'] | null>(false, {
            validators: [Validators.required]
          }),
          filter: new FormControl<SegmentedOutput['filter'] | null>(''),
          orderBy: new FormControl<SegmentedOutput['orderBy'] | null>('', {
            validators: [Validators.required, Validators.minLength(1)]
          }),
          orderAsc: new FormControl<SegmentedOutput['orderAsc'] | null>(false),
          scheduled: new FormGroup<TypedFormGroup<ScheduledPolicy>>(
            {
              cronString: new FormControl<ScheduledPolicy['cronString'] | null>(
                '',
                {
                  validators: [
                    Validators.required,
                    Validators.minLength(1),
                    Validators.maxLength(10)
                  ]
                }
              )
            },
            { validators: [Validators.required] }
          ),
          size: new FormControl<SegmentedOutput['size'] | null>(10, {
            validators: [
              Validators.required,
              Validators.min(10),
              Validators.max(100)
            ]
          })
        })
      },
      { validators: [Validators.required, Validators.minLength(1)] }
    )
  });

  // agents: Agent[] = [];

  segmentedDeliveryModalContext: SegmentedDeliveryModalContext;
  sinkTargetOptions: KeyLabelOption<SinkTargetType>[] = [
    {
      key: 'email',
      label: 'Email'
    },
    {
      key: 'webhook',
      label: 'Webhook'
    }
  ];

  visibilityOptions: KeyLabelOption<GqlVisibility>[] = [
    {
      key: GqlVisibility.IsPrivate,
      label: 'Private'
    },
    {
      key: GqlVisibility.IsPublic,
      label: 'Public'
    }
  ];
  fetchFrequencyOptions = this.getFetchFrequencyOptions();

  fields: Field[];
  // hasFields: boolean;
  fetchFrequencyFC: FormControl<string>;

  entryPlugins: KeyLabelOption<string>[];
  protected readonly CUSTOM_FETCH_FREQUENCY = 'custom';
  private subscriptions: Subscription[] = [];

  constructor(
    private readonly scrapeService: ScrapeService,
    private readonly changeRef: ChangeDetectorRef,
    private readonly modalService: ModalService,
    private readonly subscriptionService: SourceSubscriptionService,
    private readonly modalCtrl: ModalController,
    private readonly agentService: AgentService,
    private readonly plguinService: PluginService
  ) {
  }

  async ngOnInit(): Promise<void> {
    this.fetchFrequencyFC = new FormControl<string>('', { nonNullable: false });
    this.feedBuilderFg.patchValue(this.feedBuilder);

    this.feedBuilderFg.controls.sink.controls.segmented.disable();

    this.subscriptions.push(
      this.feedBuilderFg.controls.sink.controls.hasRetention.valueChanges.subscribe(
        (hasRetention) => {
          if (hasRetention) {
            this.feedBuilderFg.controls.sink.controls.retention.enable();
          } else {
            this.feedBuilderFg.controls.sink.controls.retention.disable();
          }
        }
      ),
      this.feedBuilderFg.controls.sink.controls.isSegmented.valueChanges.subscribe(
        (isSegmented) => {
          if (isSegmented) {
            this.feedBuilderFg.controls.sink.controls.segmented.enable();
          } else {
            this.feedBuilderFg.controls.sink.controls.segmented.disable();
          }
        }
      ),
      this.fetchFrequencyFC.valueChanges.subscribe((cronString) => {
        if (cronString !== this.CUSTOM_FETCH_FREQUENCY) {
          this.feedBuilderFg.controls.fetch.controls.cronString.patchValue(
            cronString
          );
        }
      }),
      this.feedBuilderFg.controls.fetch.controls.cronString.valueChanges.subscribe(
        (customCronString) => {
          const match = this.getFetchFrequencyOptions()
            .map((option) => option.key)
            .find((cronString) => cronString === customCronString.trim());

          const value = match ?? this.CUSTOM_FETCH_FREQUENCY;
          if (this.fetchFrequencyFC.value !== value) {
            this.fetchFrequencyFC.setValue(value);
          }
        }
      )
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

    // this.agents = await this.agentService.getAgents();

    this.parse(this.feedBuilder);

    const plugins = await this.plguinService.listPlugins();
    this.entryPlugins = plugins
      .filter((plugin) => plugin.type === GqlPluginType.Entity)
      .map((plugin) => {
        return {
          key: plugin.id,
          label: plugin.name
        } as KeyLabelOption<string>;
      });

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
        response: response.data.response
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

  getLabelForAgent() {
    if (
      this.feedBuilderFg.controls.agent.valid &&
      this.feedBuilderFg.value.agent
    ) {
      const agent = this.feedBuilderFg.value.agent;
      return `${agent.version}`;
    } else {
      return 'Auto';
    }
  }

  closeModal() {
    return this.modalCtrl.dismiss(this.feedBuilderFg.value);
  }

  async save() {
    // console.log(JSON.stringify(this.getFormControlStatus(this.feedBuilderFg), null, 2));

    let segmented: GqlSegmentInput = null;
    if (this.feedBuilderFg.value.sink.isSegmented) {
      segmented = {
        digest: this.feedBuilderFg.value.sink.segmented.digest,
        // todo mag filter: this.feedBuilderFg.value.sink.segmented.filter,
        orderBy: this.feedBuilderFg.value.sink.segmented.orderBy,
        orderAsc: this.feedBuilderFg.value.sink.segmented.orderAsc,
        scheduleExpression:
        this.feedBuilderFg.value.sink.segmented.scheduled.cronString,
        size: this.feedBuilderFg.value.sink.segmented.size
      };
    }

    const bucket = this.feedBuilderFg.value.sink;
    await this.subscriptionService.createSubscriptions({
      subscriptions: [
        {
          sources: this.feedBuilderFg.value.source.map(
            (source) => source.request
          ),
          sourceOptions: {
            refreshCron: this.feedBuilderFg.value.fetch.cronString
          },
          sinkOptions: {
            segmented,
            visibility: bucket.visibility,
            plugins: this.getPluginExecutions(),
            title: bucket.title,
            description: bucket.description,
            // filters: this.feedBuilderFg.value.filters.map(filter => {
            //   const toOperatorDto = (operator: FieldFilterOperator): GqlStringFilterOperator => {
            //     switch (operator) {
            //       case 'contains':
            //         return GqlStringFilterOperator.Contains;
            //       case 'matches':
            //         return GqlStringFilterOperator.Matches;
            //       case 'startsWith':
            //         return GqlStringFilterOperator.EndsWith;
            //       case 'endsWith':
            //         return GqlStringFilterOperator.StartsWidth;
            //       default:
            //         throw new Error(`Cannot map FieldFilterOperator ${operator}`);
            //     }
            //   };
            //   const fieldFilter: GqlFieldFilterConditionInput = {
            //     value: filter.value,
            //     field: filter.field,
            //     invert: filter.negate,
            //     operator: toOperatorDto(filter.operator)
            //   };
            //   if (filter.type === 'exclude') {
            //     return {
            //       exclude: fieldFilter
            //     };
            //   } else {
            //     return {
            //       include: fieldFilter
            //     };
            //   }
            // }),
            retention: {
              maxAgeDays: 0,
              maxItems: 0
            }
          },
          additionalSinks: []
        }
      ]
    });
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

  isProvidesAsciiFields(): boolean {
    return this.feedBuilderFg.controls.source.length > 0;
  }

  needsAgent(): boolean {
    return this.feedBuilderFg.value.source.some((source) =>
      isDefined(source.request.page.prerender)
    );
  }

  addFilter(data: FieldFilter = null): void {
    const filter = new FormGroup<TypedFormGroup<FieldFilter>>({
      value: new FormControl<FieldFilter['value'] | null>('', {
        nonNullable: true,
        validators: [Validators.required, Validators.minLength(3)]
      }),
      field: new FormControl<string>(null, {
        nonNullable: true,
        validators: [Validators.required]
      }),
      negate: new FormControl<boolean>(false, {
        nonNullable: true,
        validators: [Validators.required]
      }),
      type: new FormControl<FieldFilterType>('include', {
        nonNullable: true,
        validators: [Validators.required]
      }),
      operator: new FormControl<FieldFilterOperator>('contains', {
        nonNullable: true,
        validators: [Validators.required]
      })
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
    for (let i = 0; i < this.feedBuilderFg.controls.filters.length; i++) {
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
      }
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
      }
    ];
  }

  getSourcesFc(): FormControl<Source>[] {
    const filtersFg: FormControl<Source>[] = [];
    for (let i = 0; i < this.feedBuilderFg.controls.source.length; i++) {
      filtersFg.push(
        this.feedBuilderFg.controls.source.at(i) as FormControl<Source>
      );
    }
    return filtersFg;
  }

  getTargets(
    sinkFg: FormGroup<TypedFormGroup<Sink>>
  ): FormGroup<TypedFormGroup<SinkTargetWrapper>>[] {
    const targetFg: FormGroup<TypedFormGroup<SinkTargetWrapper>>[] = [];
    for (let i = 0; i < sinkFg.controls.targets.length; i++) {
      targetFg.push(sinkFg.controls.targets.at(i));
    }
    return targetFg;
  }

  async addTarget(
    sinkTargetType: SinkTargetType,
    data: SinkTargetWrapper = null
  ) {
    const sinkTarget = new FormGroup<TypedFormGroup<SinkTargetWrapper>>({
      type: new FormControl<SinkTargetType>(sinkTargetType),
      oneOf: new FormGroup<TypedFormGroup<SinkTarget>>({
        email: new FormGroup<TypedFormGroup<EmailSink>>({
          address: new FormControl<EmailSink['address'] | null>('', {
            nonNullable: true,
            validators: [Validators.required, Validators.email]
          })
        }),
        webhook: new FormGroup<TypedFormGroup<WebhookSink>>({
          url: new FormControl<WebhookSink['url'] | null>('', {
            nonNullable: true,
            validators: [Validators.required, Validators.minLength(1)]
          })
        })
      })
    });

    sinkTarget.controls.type.valueChanges.subscribe((type) => {
      Object.keys(sinkTarget.controls.oneOf.controls).forEach((otherKey) => {
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
        // case 'bucket':
        //   await this.openBucketsModal(sinkTarget)
        //     .then(() => {
        //       this.feedBuilderFg.controls.sink.controls.targets.push(sinkTarget);
        //     });
        //   break;
        default:
          this.feedBuilderFg.controls.sink.controls.targets.push(sinkTarget);
          break;
      }
    }
  }

  async openCodeEditor() {
    const originalData = cloneDeep(this.feedBuilderFg.value);
    unset(originalData, 'source.response');
    const data: FeedBuilder = await this.modalService.openCodeEditorModal(
      JSON.stringify(originalData, null, 2)
    );
    this.parse(data);
  }

  getEmitType(sourceFc: FormControl<Source>): string {
    const emitTypes = uniq(
      sourceFc.value.request.emit.map((emit) =>
        emit.selectorBased?.expose?.pixel ? 'image' : 'markup'
      )
    );
    if (emitTypes.length > 0) {
      return emitTypes.join(', ');
    } else {
      return 'raw';
    }
  }

  logFormGroupStatus(fg: FormGroup): void {
    console.log(JSON.stringify(getFormControlStatus(fg), null, 2));
  }

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
        key: '0 0 * * * *',
        label: 'Every hour'
      },
      {
        key: '0 0 */2 * * *',
        label: 'Every 2 hours'
      },
      {
        key: EVERY_FOUR_HOURS,
        label: 'Every 4 hours'
      },
      {
        key: '0 0 */8 * * *',
        label: 'Every 8 hours'
      },
      {
        key: '0 0 */12 * * *',
        label: 'Every 12 hours'
      },
      {
        key: '0 0 0 * * *',
        label: 'Every day'
      },
      {
        key: '0 0 0 */2 * *',
        label: 'Every 2 days'
      },
      {
        key: '0 0 0 * * 0',
        label: 'Every week'
      },
      {
        key: '0 0 0 1 * *',
        label: 'Every month'
      },
      {
        key: 'custom',
        label: 'Custom'
      }
    ];
  }

  private parse(data: FeedBuilder) {
    if (data) {
      data.sources?.forEach((source) => this.addSource(source));
      this.feedBuilderFg.controls.agent.patchValue(data.agent);
      this.feedBuilderFg.controls.fetch.patchValue(data.fetch);
      this.feedBuilderFg.controls.sink.patchValue(data.sink);
      if (data.sink) {
        this.feedBuilderFg.controls.sink.controls.isSegmented.setValue(
          !!data.sink.segmented
        );
        data.sink.targets?.map((target) => this.addTarget(target.type, target));
      }
      data.filters?.forEach((filter) => this.addFilter(filter));
    }
    this.changeRef.detectChanges();
  }

  private async addSource(source: Source) {
    const sourceFg = new FormControl<Source>(null);
    if (source) {
      sourceFg.setValue(source);
      if (!source.response) {
        this.scrapeService
          .scrape(omit(source.request, 'id', 'corrId'))
          .then((response) => {
            source.response = response;
            this.changeRef.detectChanges();
          });
      }
    }
    this.feedBuilderFg.controls.source.push(sourceFg);
  }

  private getPluginExecutions(): GqlPluginExecutionInput[] {
    return this.feedBuilderFg.value.fetch.plugins.map((plugin) => {
      return {
        pluginId: plugin.id,
        params: {
          rawJson: '' + plugin.data
        }
      };
    });
  }
}

export function getFormControlStatus(fc: FormControl | FormGroup | FormArray) {
  const base = {
    __valid: fc.valid,
    __enabled: fc.enabled
  };
  if (fc.enabled) {
    if (fc instanceof FormControl) {
      return {
        ...base,
        __value: JSON.stringify(fc.value)?.substring(0, 10)
      };
    }
    if (fc instanceof FormGroup) {
      return {
        ...base,
        map: Object.keys(fc.controls).reduce((agg, k) => {
          agg[k] = getFormControlStatus(fc.controls[k] as any);
          return agg;
        }, {})
      };
    }
    if (fc instanceof FormArray) {
      const list = [];
      for (let i = 0; i < fc.length; i++) {
        list.push(getFormControlStatus(fc.at(i) as any));
      }
      return {
        ...base,
        list
      };
    }
  } else {
    return base;
  }
}
