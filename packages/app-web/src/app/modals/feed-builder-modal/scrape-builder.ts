import {
  GqlAgentInput,
  GqlBucketCreateInput,
  GqlBucketWhereInput,
  GqlScrapeRequestInput
} from '../../../generated/graphql';
import { ScrapeResponse } from '../../graphql/types';
import { isEqual, isNull, isUndefined, uniq, without } from 'lodash-es';
import { KeyLabelOption } from '../../components/select/select.component';
import { ScrapeService } from '../../services/scrape.service';
import { NativeOrGenericFeed } from '../../components/transform-website-to-feed/transform-website-to-feed.component';
import { Agent } from '../../services/agent.service';
import { Subject } from 'rxjs';

export type Maybe<T> = T | null

export type OutputType = 'markup' | 'text' | 'image' | 'feed' | 'other'

export interface Field {
  type: 'text' | 'markup' | 'base64' | 'url' | 'date'
  name: string
}

export function isDefined(v: any | undefined): boolean {
  return !isNull(v) && !isUndefined(v);
}

const fieldsInFeed: Field[] = [
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


interface Artefact {
  type: OutputType
  fields: Field[]
  label: string
}

interface Provider {
  produces(): Artefact[];
}

abstract class Builder<T, S> implements Provider {
  constructor(readonly parent: T,
              readonly spec: Maybe<S>) {
    if (spec) {
      setTimeout(() => {
        this.init(spec);
      }, 1);
    }
  }

  abstract init(spec: Maybe<S>): void

  abstract build(): S

  abstract notifyChange(): void;

  abstract produces(): Artefact[]
}

export type ResponseMapper = 'feed' | 'fragment'

export class ResponseMapperBuilder extends Builder<SourceBuilder, ResponseMapperBuilderSpec> {

  implementation?: ResponseMapperBuilderSpec;

  init(spec: Maybe<ResponseMapperBuilderSpec>): void {
    this.implementation = spec;
    this.notifyChange();
  }

  notifyChange(): void {
    this.parent.notifyChange();
  }

  produces(): Artefact[] {
    const abort = () => {
      throw new Error('not supported');
    };
    if (this.implementation) {
      if (this.implementation.feed) {
        if (this.implementation.feed.genericFeed) {
          return [
            {
              type: 'feed',
              label: 'Generic Feed',
              fields: fieldsInFeed
            }
          ];
        } else {
          if (this.implementation.feed.nativeFeed) {
            return [
              {
                type: 'feed',
                label: 'Native Feed',
                fields: fieldsInFeed
              }
            ];
          } else {
            return [
              {
                type: 'other',
                label: 'Feed...',
                fields: []
              }
            ];
          }
        }
      } else {
        if (this.implementation.fragment) {
          if (this.implementation.fragment.pixel) {
            return [
              {
                type: 'image',
                label: 'Image',
                fields: []
              }
            ];
          } else {
            if (this.implementation.fragment.xpath) {
              return [
                {
                  type: 'feed',
                  label: 'Text Fragment',
                  fields: []
                }
              ];
            } else {
              abort();
            }
          }
        } else {
          abort();
        }
      }
    } else {
      throw abort;
    }
  }

  build(): ResponseMapperBuilderSpec {
    return this.implementation;
  }

  isValid(): boolean {
    return isDefined(this.implementation) && (
      isDefined(this.implementation.feed) && !isEqual(this.implementation.feed, {})
       || isDefined(this.implementation.fragment) && !isEqual(this.implementation.fragment, {})
    )
  }
}

function isFeed(contentType: string): boolean {
  return contentType && [
    'application/atom+xml',
    'application/rss+xml',
    'application/xml',
    'text/xml'
  ].some(feedMime => contentType.toLowerCase().startsWith(feedMime));
}

function mapMimeToProviderType(contentType: string): Artefact[] {
    if (contentType) {
      if (isFeed(contentType)) {
        return [{ type: 'feed', label: 'Feed', fields: fieldsInFeed }]
      }
      if (contentType.startsWith('image/')) {
        return [{ type: 'image', label: 'Image', fields: [] }]
      }
      if (contentType.startsWith('text/html')) {
        return [{ type: 'markup', label: 'Markup', fields: [] }]
      }
    }
    return []
}

export class SourceBuilder extends Builder<SourcesBuilder, SourceBuilderSpec> {
  request: GqlScrapeRequestInput;
  response?: ScrapeResponse;

  readonly responseMapper: ResponseMapperBuilder = new ResponseMapperBuilder(this, null);
  pending: boolean;
  error: boolean = false;

  constructor(parent: SourcesBuilder,
              private readonly scrapeService: ScrapeService,
              spec: Maybe<SourceBuilderSpec>) {
    super(parent, spec);
  }

  async init(spec: Maybe<SourceBuilderSpec>): Promise<void> {
    if (spec) {
      this.responseMapper.init(spec.responseMapper);
      this.request = spec.request;
      this.pending = true;
      this.notifyChange();
      this.scrapeService.scrape(spec.request)
        .then(response => {
          this.response = response;
          this.pending = false;
          this.notifyChange();
        })
        .catch((e) => {
          console.log(e);
          this.pending = false;
          this.error = true;
          this.notifyChange();
        });
    }
  }

  notifyChange(): void {
    this.parent.notifyChange();
  }

  produces(): Artefact[] {
    if (this.responseMapper && this.responseMapper.implementation) {
      return this.responseMapper.produces();
    } else {
      return mapMimeToProviderType(this.response?.debug?.contentType);
    }
  }

  build(): SourceBuilderSpec {
    return {
      request: this.request,
      responseMapper: this.responseMapper.build()
    };
  }

  deleteResourceMapper() {
    this.responseMapper.implementation = null;
    this.notifyChange();
  }

  getResponseMapperType(): ResponseMapper {
    if (this.responseMapper.implementation.fragment) {
      return 'fragment';
    } else {
      if (this.responseMapper.implementation.feed) {
        return 'feed';
      } else {
        throw new Error('not supported');
      }
    }
  }

  withMapper(responseMapper: ResponseMapperBuilderSpec) {
    this.responseMapper.implementation = responseMapper;
    this.notifyChange();
  }

  isResponseMapperValid() {
    return isDefined(this.responseMapper) && this.responseMapper.isValid();
  }

  hasResourceMapper() {
    return isDefined(this.responseMapper.implementation)
  }
}

interface SourceBuilderSpec {
  request: GqlScrapeRequestInput;
  responseMapper?: ResponseMapperBuilderSpec;
}

interface PixelFragmentMapperBuilderSpec {

}

interface XpathFragmentMapperBuilderSpec {

}

interface FragmentMapperBuilderSpec {
  pixel: PixelFragmentMapperBuilderSpec;
  xpath: XpathFragmentMapperBuilderSpec;
}

interface ResponseMapperBuilderSpec {
  feed?: NativeOrGenericFeed;
  fragment?: FragmentMapperBuilderSpec;
}

interface FeedFilterSpec {
  type: 'include' | 'exclude'
  field: 'title' | 'description' | 'link'
  negate: '-' | 'not'
  operator: 'contains' | 'endsWith' | 'startsWith'
  value: string
}

export interface ScrapeBuilderSpec {
  sources?: SourceBuilderSpec[];
  agent?: GqlAgentInput;
  fetch?: FetchPolicySpec;
  filters?: FeedFilterSpec[]
  sink?: SinkSpec
}

class SourcesBuilder extends Builder<ScrapeBuilder, SourceBuilderSpec[]> {
  sources: SourceBuilder[] = [];

  constructor(parent: ScrapeBuilder,
              private readonly scrapeService: ScrapeService,
              spec: Maybe<SourceBuilderSpec[]>) {
    super(parent, spec);
  }

  add(spec: SourceBuilderSpec = null) {
    const sourceBuilder = new SourceBuilder(this, this.scrapeService, spec);
    this.sources.push(sourceBuilder);
    this.notifyChange();
    return sourceBuilder;
  }

  init(spec: Maybe<SourceBuilderSpec[]>): void {
    spec?.map(s => this.add(s));
    this.notifyChange();
  }

  notifyChange(): void {
    this.parent.notifyChange();
  }

  produces(): Artefact[] {
    return this.sources.flatMap(source => source.produces());
  }

  build(): SourceBuilderSpec[] {
    return this.sources.map(source => source.build());
  }

  canAddSource() {
    return this.sources.length < 4
  }

  needsResourceMapper(source: SourceBuilder) {
    if (this.sources.length === 1) {
      return true
    } else {
      return uniq(this.sources.map(source => source.produces().flatMap(a => a.type))).length !== 1
    }
  }

  getMapperOptions(): KeyLabelOption<ResponseMapper>[] {
    return [
      {
        key: 'feed',
        label: 'Feed'
      },
      {
        key: 'fragment',
        label: 'Fragment'
      }
    ];
  }

  // needsGroupBy() {
  //   // return this.isFeed() && this.sources.length > 1
  //   return false;
  // }

  isFeed() {
    return this.produces().map(a => a.type).includes('feed')
  }

  deleteSource(source: SourceBuilder) {
    this.sources = without(this.sources, source);
    this.notifyChange();
  }
}

export interface SinkTargetSpec {
  email?: {
    address: string
  },
  webhook?: {
    url: string
  },
  feed?: {
    existing?: Partial<GqlBucketWhereInput>
    create?: Partial<GqlBucketCreateInput>
  }
}

export interface SegmentedOutputSpec {
  filter?: {
    createdAt: {
      gt: {
        value: string
      }
    }
  },
  orderBy?: Field,
  orderAsc?: boolean,
  size?: number,
  digest?: boolean
  scheduled?: [

  ]
}

export interface SinkSpec {
  segmented?: SegmentedOutputSpec,
  targets: SinkTargetSpec[]
}

interface RefineSpec {
  create?: {
    field?: Field
    regex?: string
    aliasAs?: string
  },
  update?: {
    field?: Field
    regex?: string
    replacement?: string
  }
}

interface FetchPolicySpec {
  every: {
    minutes: number | string
  }
}

export class ScrapeBuilder {
  sources: SourcesBuilder;
  agent: GqlAgentInput;
  filters: FeedFilterSpec[] = [];
  sink: SinkSpec = {
    segmented: null,
    targets: [
      {
        feed: {

        }
      }
    ]
  };
  refines: RefineSpec[] = []
  fetch: FetchPolicySpec = {
    every: {
      minutes: 'auto'
    }
  };

  valueChanges = new Subject();

  constructor(scrapeService: ScrapeService,
              spec: Maybe<ScrapeBuilderSpec> = null) {
    this.sources = new SourcesBuilder(this, scrapeService, spec?.sources);
    if (spec && spec.sink) {
      this.sink = spec.sink;
    }
  }

  build(): ScrapeBuilderSpec {
    return {
      sources: this.sources.build(),
      agent: this.agent,
      fetch: this.fetch,
      filters: this.filters,
      sink: this.sink
    };
  }

  deleteRefine(refine: RefineSpec) {
    this.refines = without(this.refines, refine);
    this.notifyChange();
  }

  addRefine(refine: RefineSpec = null) {
    if (refine) {
      this.refines.push(refine)
    } else {
      this.refines.push({
        update: {
        }
      })
    }
    this.notifyChange();
  }

  addFilter(filter: FeedFilterSpec = null) {
    if (filter) {
      this.filters.push(filter)
    } else {
      this.filters.push({
        field: 'title',
        negate: '-',
        operator: 'contains',
        type: 'include',
        value: ''
      })
    }
    this.notifyChange();
  }

  deleteFilter(filter: FeedFilterSpec) {
    this.filters = without(this.filters, filter);
    this.notifyChange();
  }

  notifyChange() {
    this.valueChanges.next(true);
  }

  canAddFilter() {
    return this.filters.length < 4;
  }

  produces(): Artefact[] {
    return this.sources.produces()
  }

  canAddRefine() {
    return this.refines.length < 2
  }

  isProvidesFields() {
    return this.sources.produces()
      .filter(a => a.fields)
      .flatMap(a => a.fields).length > 0
  }

  removeTargetInSink(target: SinkTargetSpec, sink: SinkSpec) {
    sink.targets = without(sink.targets, target);
    this.notifyChange();
  }

  setAgent(agent: Agent) {
    this.agent = agent;
    this.notifyChange();
  }

  hasAgent() {
    return !!this.agent;
  }

  needsAgent(): boolean {
    return this.sources.sources.some(source => !!source.request?.page?.prerender) || this.sources.sources.some(source => source.produces().some(it => it?.type === 'image'));
  }

  hasSegmentedSink(): boolean {
    return isDefined(this.sink.segmented) && !isEqual(this.sink.segmented, {})
  }

  isSegmentedSinkValid(): boolean {
    return false;
  }
}
