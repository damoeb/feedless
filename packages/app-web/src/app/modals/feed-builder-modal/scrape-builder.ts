import {
  GqlAgentInput,
  GqlBucketCreateInput,
  GqlBucketWhereInput,
  GqlScrapeRequestInput
} from '../../../generated/graphql';
import { ScrapeResponse } from '../../graphql/types';
import { isEqual, without } from 'lodash-es';
import { KeyLabelOption } from '../../components/select/select.component';
import { ScrapeService } from '../../services/scrape.service';
import { NativeOrGenericFeed } from '../../components/transform-website-to-feed/transform-website-to-feed.component';
import { Agent } from '../../services/agent.service';
import { Subject } from 'rxjs';

export type Maybe<T> = T | null

export type OutputType = 'markup' | 'text' | 'image' | 'feed'

interface Field {
  type: 'text' | 'markup' | 'base64' | 'url' | 'date'
  name: string
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

  responseMapper?: ResponseMapperBuilderSpec;

  init(spec: Maybe<ResponseMapperBuilderSpec>): void {
    this.responseMapper = spec;
    this.notifyChange();
  }

  notifyChange(): void {
    this.parent.notifyChange();
  }

  produces(): Artefact[] {
    const abort = () => {
      throw new Error('not supported');
    };
    if (this.responseMapper) {
      if (this.responseMapper.feed) {
        if (this.responseMapper.feed.genericFeed) {
          return [
            {
              type: 'feed',
              label: 'Generic Feed',
              fields: fieldsInFeed
            }
          ];
        } else {
          if (this.responseMapper.feed.nativeFeed) {
            return [
              {
                type: 'feed',
                label: 'Native Feed',
                fields: fieldsInFeed
              }
            ];
          } else {
            abort();
          }
        }
      } else {
        if (this.responseMapper.fragment) {
          if (this.responseMapper.fragment.pixel) {
            return [
              {
                type: 'image',
                label: 'Image',
                fields: []
              }
            ];
          } else {
            if (this.responseMapper.fragment.xpath) {
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
    return this.responseMapper;
  }

  hasMapper() {
    try {
      this.produces()
    } catch (e) {
      return false;
    }
    return true
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

  mapper: ResponseMapperBuilder = new ResponseMapperBuilder(this, null);

  constructor(parent: SourcesBuilder,
              private readonly scrapeService: ScrapeService,
              spec: Maybe<SourceBuilderSpec>) {
    super(parent, spec);
  }

  async init(spec: Maybe<SourceBuilderSpec>): Promise<void> {
    if (spec) {
      this.mapper = new ResponseMapperBuilder(this, spec.mapper);
      this.request = spec.request;
      this.scrapeService.scrape(spec.request).then(response => {
        this.response = response;
        this.notifyChange();
      });
    }
  }

  notifyChange(): void {
    this.parent.notifyChange();
  }

  produces(): Artefact[] {
    if (this.mapper && this.mapper.responseMapper) {
      return this.mapper.produces();
    } else {
      return mapMimeToProviderType(this.response?.debug?.contentType);
    }
  }

  build(): SourceBuilderSpec {
    return {
      request: this.request,
      mapper: this.mapper.build()
    };
  }

  deleteMapper() {
    this.mapper = null;
    this.notifyChange();
  }

  getResponseMapperType(): ResponseMapper {
    if (this.mapper.responseMapper.fragment) {
      return 'fragment';
    } else {
      if (this.mapper.responseMapper.feed) {
        return 'feed';
      } else {
        throw new Error('not supported');
      }
    }
  }

  withMapper(mapper: ResponseMapperBuilderSpec) {
    if (!this.mapper) {
      this.mapper = new ResponseMapperBuilder(this, null)
    }
    this.mapper.responseMapper = mapper;
    this.notifyChange();
  }
}

interface SourceBuilderSpec {
  request: GqlScrapeRequestInput;
  mapper?: ResponseMapperBuilderSpec;
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
  filters?: FeedFilterSpec[]
}

class SourcesBuilder extends Builder<ScrapeBuilder, SourceBuilderSpec[]> {
  sources: SourceBuilder[] = [];
  agent: Agent;
  frequencyMin: number;

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

  needsAgent(): boolean {
    return this.sources.some(source => !!source.request?.page?.prerender) || this.sources.some(source => source.produces().some(it => it?.type === 'image'));
  }

  hasAgent() {
    return !!this.agent;
  }

  canAddSource() {
    return this.sources.length < 4
  }

  needsMapper(source: SourceBuilder) {
    const others = without(this.sources, source);
    if (others.length === 0) {
      return true
    } else {
      const sourceArtefacts = source.produces().map(a => a.type)
      const otherArtefacts = others.flatMap(otherSource => otherSource.produces()).map(a => a.type)
      return !isEqual(sourceArtefacts, otherArtefacts);
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

export interface SinkSpec {
  scoped?: {
    filter?: {
      createdAt: {
        gt: {
          value: string
        }
      }
    },
    orderBy?: {
      field: Field
      asc: boolean
    },
    limit?: number,
    reduceToDigest?: boolean
    scheduled?: [

    ]
  },
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

export class ScrapeBuilder {
  sources: SourcesBuilder;
  agent: GqlAgentInput;
  filters: FeedFilterSpec[] = [];
  sinks: SinkSpec[] = [];
  refines: RefineSpec[] = []

  valueChanges = new Subject();

  constructor(scrapeService: ScrapeService,
              spec: Maybe<ScrapeBuilderSpec> = null) {
    this.sources = new SourcesBuilder(this, scrapeService, spec?.sources);
  }

  build(): ScrapeBuilderSpec {
    return {
      sources: this.sources.build(),
      agent: this.agent,
      filters: this.filters
    };
  }

  addSink(sink: SinkSpec = null) {
    if (sink) {
      this.sinks.push(sink)
    } else {
      this.sinks.push({
        scoped: null,
        targets: [
          {
            feed: {

            }
          }
        ]
      })
    }
    this.notifyChange();
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

  deleteSink(sink: SinkSpec) {
    this.sinks = without(this.sinks, sink);
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
}
