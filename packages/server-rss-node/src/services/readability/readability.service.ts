import { Injectable, Logger } from '@nestjs/common';
import { Readability } from 'mozilla-readability';
import { JSDOM } from 'jsdom';
import fetch, { Response } from 'node-fetch';
import {
  EventType,
  MessageBrokerService,
} from '../messageBroker/messageBroker.service';

@Injectable()
export class ReadabilityService {
  private readonly logger = new Logger(ReadabilityService.name);

  constructor(readonly messageBroker: MessageBrokerService) {
    messageBroker.subscribe<string>(EventType.readability, async (url) => {
      const readability = await this.getReadability(url);
      if (readability) {
        this.logger.debug(`ok -> parseReadability url=${url}`);
        messageBroker.publish(EventType.readabilityParsed, {
          url,
          readability,
        });
      } else {
        this.logger.debug(`not ok -> parseReadability url=${url}`);
        messageBroker.publish(EventType.readabilityFailed, { url });
      }
    });

    // setTimeout(() => {
    //   messageBroker.publish(EventType.readability, 'http://telepolis.de');
    // }, 3000);
  }

  async getReadability(url: string): Promise<any> {
    const response: Response = await fetch(url);
    if (response.status === 200) {
      const body = await response.text();

      const dom = new JSDOM(body as any);
      const parser = new Readability(dom.window.document);
      const readability = parser.parse();
      if (readability) {
        return readability;
      }
    }
  }
}
