import { Injectable, Logger } from '@nestjs/common';
import { Readability } from 'mozilla-readability';
import { JSDOM } from 'jsdom';
import fetch, { Response } from 'node-fetch';
import { MessageBrokerService } from '../message-broker/message-broker.service';
import {
  MqAskReadability,
  MqOperation,
  MqReadability,
} from '../../generated/mq';
import { PuppeteerService } from '../puppeteer/puppeteer.service';

@Injectable()
export class ReadabilityService {
  private readonly logger = new Logger(ReadabilityService.name);

  constructor(
    readonly messageBroker: MessageBrokerService,
    private readonly puppeteer: PuppeteerService,
  ) {
    messageBroker.subscribe<MqAskReadability>(
      MqOperation.AskReadability,
      async ({ url }) => {
        this.logger.log(`Extracting readability ${url}`);
        try {
          const readability = await this.getReadability(url);
          if (readability) {
            messageBroker.publish<MqReadability>(MqOperation.Readability, {
              url,
              error: false,
              readability,
            });
          } else {
            messageBroker.publish<MqReadability>(MqOperation.Readability, {
              url,
              error: true,
            });
          }
        } catch (e) {
          this.logger.log(`Failed readability ${url}: ${e.message}`);
          messageBroker.publish<MqReadability>(MqOperation.Readability, {
            url,
            error: true,
          });
        }
      },
    );
  }

  async getReadability(url: string, dynamic = true): Promise<any> {
    const body = await this.getBody(url, dynamic);

    const dom = new JSDOM(body as any);
    const parser = new Readability(dom.window.document);
    const readability = parser.parse();
    if (readability) {
      return readability;
    }
  }

  private async getBody(url: string, dynamic: boolean) {
    if (dynamic) {
      return this.puppeteer.getMarkup(url, 7000);
    } else {
      const response: Response = await fetch(url);
      if (response.status === 200) {
        return response.text();
      }
      throw new Error(`Invalid status ${response.status}`);
    }
  }
}
