import { Component, OnInit, ViewChild } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { GqlScrapeEmitType, GqlScrapeRequestInput } from '../../../generated/graphql';
import { ScrapeService } from '../../services/scrape.service';
import { ScrapeResponse } from '../../graphql/types';
import { AppSelectOption } from '../../components/select/select.component';
import { NativeOrGenericFeed } from '../../components/transform-website-to-feed/transform-website-to-feed.component';
import { without } from 'lodash-es';

// interface BuilderStep<T> {
//   data: T
//   error?: boolean
//   errorMessage?: string
// }

interface FromSpec {
  scrapeRequest: GqlScrapeRequestInput,
  scrapeResponse?: ScrapeResponse,
  transform?: NativeOrGenericFeed
}

interface ScrapeSourceModalContext {
  request?: GqlScrapeRequestInput
  response?: ScrapeResponse
  fromSpec?: FromSpec
}

interface WebsiteToFeedModalContext {
  feed?: NativeOrGenericFeed;
  request: GqlScrapeRequestInput
  response: ScrapeResponse
  fromSpec: FromSpec
}

interface WhereSpec {
  field: 'title' | 'description' | 'link'
  negate: '-' | 'not'
  operator: 'contains' | 'endsWith' | 'startsWith'
  value: string
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

  selectSpec: string;
  fromSpecs: FromSpec[] = [
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
  ]

  whereSpecs: WhereSpec[] = []

  scrapeSourceModalContext: ScrapeSourceModalContext = {};
  websiteToFeedModalContext: WebsiteToFeedModalContext;

  constructor(readonly modalCtrl: ModalController,
              private readonly scrapeService: ScrapeService) {}

  async ngOnInit() {
    await Promise.all(this.fromSpecs.filter(fromSpec => !fromSpec.scrapeResponse)
      .map(fromSpec => this.fixFromSpec(fromSpec)));
  }

  closeModal() {
    return this.modalCtrl.dismiss();
  }

  getScrapeUrl(fromSpec: FromSpec): string {
    return fromSpec.scrapeRequest.page.url.replace(/http[s]?:\/\//, '');
  }

  getNotes(fromSpec: FromSpec): string {
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

  needsTransform(fromSpec: FromSpec): boolean {
    // const mime = fromSpec.scrapeResponse.debug.contentType.split(';')[0].toLowerCase();
    // if (this.selectSpec === '')
    // return [].includes(this.selectSpec)
    return true
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

  private async fixFromSpec(fromSpec: FromSpec) {
    fromSpec.scrapeResponse = await this.scrapeService.scrape(fromSpec.scrapeRequest)
  }

  async dismissScrapeSourceModal() {
    if (this.scrapeSourceModalContext) {
      const {request, response, fromSpec} = this.scrapeSourceModalContext;
      if (fromSpec) {
        fromSpec.scrapeRequest = request;
        fromSpec.scrapeResponse = response;
      } else {
        this.fromSpecs.push({
          scrapeRequest: request,
          scrapeResponse: response
        })
      }
    }
    this.scrapeSourceModalContext = null;
    await this.scrapeSourceModalElement.dismiss()
  }

  async openScrapeSourceModal(fromSpec?: FromSpec) {
    this.scrapeSourceModalContext = {
      request: fromSpec?.scrapeRequest,
      response: fromSpec?.scrapeResponse,
      fromSpec
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

  async openWebsiteToFeedModal(fromSpec: FromSpec) {
    this.websiteToFeedModalContext = {
      request: fromSpec.scrapeRequest,
      response: fromSpec.scrapeResponse,
      fromSpec
    }
    await this.websiteToFeedModalElement.present()
  }

  getLabelForTransformation({ transform }: FromSpec): string {
    if (transform.genericFeed) {
      return 'Generic Feed';
    } else if (transform.nativeFeed) {
      return 'Native Feed';
    } else {
      throw new Error('not supported')
    }
  }

  deleteFromSpec(fromSpec: FromSpec) {
    this.fromSpecs = without(this.fromSpecs, fromSpec);
  }

  addWhereSpec() {
    this.whereSpecs.push({
      field: 'title',
      negate: '-',
      operator: 'contains',
      value: ''
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
}
