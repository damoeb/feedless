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

@Injectable()
export class ReadabilityService {
  private readonly logger = new Logger(ReadabilityService.name);

  constructor(readonly messageBroker: MessageBrokerService) {
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
