import { Component, OnInit, ViewChild } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { GqlScrapeEmitType, GqlScrapeRequestInput } from '../../../generated/graphql';
import { ScrapeService } from '../../services/scrape.service';
import { ScrapeResponse } from '../../graphql/types';
import { AppSelectOption } from '../../components/select/select.component';
import { NativeOrGenericFeed } from '../../components/transform-website-to-feed/transform-website-to-feed.component';
import { without } from 'lodash-es';
import { Agent, AgentService } from '../../services/agent.service';

// interface BuilderStep<T> {
//   data: T
//   error?: boolean
//   errorMessage?: string
// }

interface FromSpecSource {
  scrapeRequest: GqlScrapeRequestInput,
  scrapeResponse?: ScrapeResponse,
  transform?: NativeOrGenericFeed
}

interface ScrapeSourceModalContext {
  request?: GqlScrapeRequestInput
  response?: ScrapeResponse
  fromSpec?: FromSpecSource
}

interface WebsiteToFeedModalContext {
  feed?: NativeOrGenericFeed;
  request: GqlScrapeRequestInput
  response: ScrapeResponse
  fromSpec: FromSpecSource
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

interface SinkSpec {
  targets: SinkTarget[];
  scope: 'all' | 'throlled';
}

function isFeed(contentType: string): boolean {
  console.log('contentType', contentType)
  return contentType && [
    'application/atom+xml',
    'application/rss+xml',
    'application/xml',
    'text/xml'
  ].some(feedMime => contentType.toLowerCase().startsWith(feedMime));
}

interface FromSpec {
  agent?: Agent;
  sources: FromSpecSource[]
}

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

  selectSpec: 'pixel' | 'html' | 'text' | 'feed';
  fromSpec: FromSpec =
    {
      sources: [
        {
          scrapeRequest: {
            page: {
              url: 'https://heise.de'
            },
            emit: [GqlScrapeEmitType.Feeds],
            elements: ['/'],
            debug: {
              html: true
            }
          }
        }
      ],
    };

  whereSpecs: WhereSpec[] = [];

  sinkSpecs: SinkSpec[] = [];

  scrapeSourceModalContext: ScrapeSourceModalContext = {};
  websiteToFeedModalContext: WebsiteToFeedModalContext;

  constructor(readonly modalCtrl: ModalController,
              private readonly scrapeService: ScrapeService,
              private readonly agentService: AgentService) {}

  async ngOnInit() {
    await Promise.all(this.fromSpec.sources.filter(source => !source.scrapeResponse)
      .map(source => this.fixSource(source)));
  }

  closeModal() {
    return this.modalCtrl.dismiss();
  }

  getScrapeUrl(fromSpec: FromSpecSource): string {
    return fromSpec.scrapeRequest.page.url.replace(/http[s]?:\/\//, '');
  }

  getNotes(fromSpec: FromSpecSource): string {
    let engine;
    if (fromSpec.scrapeRequest.page.prerender) {
      const actionsCount = fromSpec.scrapeRequest.page.actions?.length || 0;
      engine = `chome, ${actionsCount} actions`
    } else {
      engine = 'static'
    }

    let responseType: string;
    if (fromSpec.scrapeResponse) {
      responseType = fromSpec.scrapeResponse.debug.contentType
    } else {
      responseType = '...'
    }
    return `${engine} ${responseType}`;
  }

  getOptionsForSelect(): AppSelectOption[] {
    return [
      {
        value: 'pixel',
        label: 'Pixel',
      },
      {
        value: 'html',
        label: 'Html',
      },
      {
        value: 'text',
        label: 'Text',
      },
      {
        value: 'feed',
        label: 'Feed',
      }
    ]
  }

  private async fixSource(source: FromSpecSource) {
    source.scrapeResponse = await this.scrapeService.scrape(source.scrapeRequest)
  }

  async dismissScrapeSourceModal() {
    if (this.scrapeSourceModalContext) {
      const {request, response, fromSpec} = this.scrapeSourceModalContext;
      if (fromSpec) {
        fromSpec.scrapeRequest = request;
        fromSpec.scrapeResponse = response;
      } else {
        this.fromSpec.sources.push({
          scrapeRequest: request,
          scrapeResponse: response
        })
      }
    }
    this.scrapeSourceModalContext = null;
    await this.scrapeSourceModalElement.dismiss()
  }

  async openScrapeSourceModal(source?: FromSpecSource) {
    this.scrapeSourceModalContext = {
      request: source?.scrapeRequest,
      response: source?.scrapeResponse,
      fromSpec: source
    }
    await this.scrapeSourceModalElement.present()
  }

  async dismissWebsiteToFeedModal() {
    if (this.websiteToFeedModalContext) {
      const {  fromSpec, feed } = this.websiteToFeedModalContext;
      fromSpec.transform = feed;
    }
    this.websiteToFeedModalContext = null;
    await this.websiteToFeedModalElement.dismiss()
  }

  async openWebsiteToFeedModal(fromSpec: FromSpecSource) {
    this.websiteToFeedModalContext = {
      request: fromSpec.scrapeRequest,
      response: fromSpec.scrapeResponse,
      fromSpec
    }
    await this.websiteToFeedModalElement.present()
  }

  getLabelForTransformation({ transform }: FromSpecSource): string {
    if (transform.genericFeed) {
      return 'Generic Feed';
    } else if (transform.nativeFeed) {
      return 'Native Feed';
    } else {
      throw new Error('not supported')
    }
  }

  deleteFromSpec(source: FromSpecSource) {
    this.fromSpec.sources = without(this.fromSpec.sources, source);
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

  getFetchFrequencyOptions(): AppSelectOption[] {
    const hour = 60;
    const day = 24 * hour;
    return [
      {
        value: hour,
        label: 'Every hour'
      },
      {
        value: 2 * hour,
        label: 'Every 2 hours'
      },
      {
        value: 4 * hour,
        label: 'Every 4 hours'
      },
      {
        value: 8 * hour,
        label: 'Every 8 hours'
      },
      {
        value: 12 * hour,
        label: 'Every 12 hours'
      },
      {
        value: day,
        label: 'Every day'
      },
      {
        value: 2 * day,
        label: 'Every 2 days'
      },
      {
        value: 7 * day,
        label: 'Every week'
      },
      {
        value: 28 * day,
        label: 'Every month'
      }
    ];
  }

  build() {

  }

  deleteWhereSpec(whereSpec: WhereSpec) {
    this.whereSpecs = without(this.whereSpecs, whereSpec);
  }

  needsTransform(fromSpec: FromSpecSource): boolean {
    return this.selectSpec === 'pixel' ||
      this.selectSpec === 'feed' && fromSpec.scrapeResponse && !isFeed(fromSpec.scrapeResponse.debug.contentType)
  }

  needsAgents(): boolean {
    return this.selectSpec === 'pixel' ||
      this.fromSpec.sources.some(source => !!source.scrapeRequest.page.prerender);
  }

  getAgents(): AppSelectOption[] {
    const agents: AppSelectOption[] = this.agentService.getAgents()
      .map(agent => ({
        label: `${agent.name}${agent.personal ? ' (personal)' : ''}`,
        value: agent.id
      }))
    return [
      ...agents,
      {
        label: 'Create new agent...',
        value: 'new'
      }
    ];
  }

  dismissFeedModal() {

  }
}
