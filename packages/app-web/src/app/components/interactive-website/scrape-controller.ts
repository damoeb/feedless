import { EventEmitter } from '@angular/core';
import {
  GqlScrapeActionInput,
  GqlScrapeRequestInput,
} from '../../../generated/graphql';
import {
  BoundingBox,
  XyPosition,
} from '../embedded-image/embedded-image.component';
import { ScrapeResponse } from '../../graphql/types';
import { getFirstFetchUrlLiteral } from '../../utils';

export class ScrapeController {
  pickPoint: EventEmitter<(position: XyPosition) => void> = new EventEmitter<
    (position: XyPosition) => void
  >();
  pickElement: EventEmitter<(xpath: string) => void> = new EventEmitter<
    (xpath: string) => void
  >();
  pickArea: EventEmitter<(bbox: BoundingBox) => void> = new EventEmitter<
    (bbox: BoundingBox) => void
  >();
  actionsChanges = new EventEmitter<void>();
  extractElements = new EventEmitter<{
    xpath: string;
    callback: (elements: HTMLElement[]) => void;
  }>();
  showElements = new EventEmitter<string>();
  cancel = new EventEmitter<void>();
  // actions: GqlScrapeActionInput[] = [];
  response: ScrapeResponse;

  constructor(public scrapeRequest: GqlScrapeRequestInput) {}

  getScrapeRequest(
    append: Array<GqlScrapeActionInput> = null,
  ): GqlScrapeRequestInput {
    return {
      title: this.scrapeRequest.title,
      tags: this.scrapeRequest.tags,
      localized: this.scrapeRequest.localized,
      flow: {
        sequence: [
          ...this.scrapeRequest.flow.sequence,
          ...(append ? append : []),
        ],
      },
    };
  }

  getUrl() {
    return getFirstFetchUrlLiteral(this.scrapeRequest.flow.sequence);
  }
}
