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
      async ({ url, prerender, correlationId, allowHarvestFailure }) => {
        const cid = correlationId;
        try {
          const { body, mime, readability } = await this.getReadability(
            cid,
            prerender,
            url,
          );
          if (readability) {
            this.logger.log(
              `[${cid}] Extracted readability (prerender=${prerender}) ${url}`,
            );
            messageBroker.publish<MqReadability>(MqOperation.Readability, {
              url,
              harvestFailed: false,
              readabilityFailed: false,
              readability,
              correlationId,
              allowHarvestFailure,
              prerender,
              contentRaw: body,
              contentRawMime: mime,
            });
          } else {
            messageBroker.publish<MqReadability>(MqOperation.Readability, {
              url,
              correlationId,
              harvestFailed: false,
              readabilityFailed: true,
              allowHarvestFailure,
              prerender,
              contentRaw: body,
              contentRawMime: mime,
            });
          }
        } catch (e) {
          this.logger.log(`[${cid}] Failed readability: ${e.message}`);
          messageBroker.publish<MqReadability>(MqOperation.Readability, {
            url,
            correlationId,
            harvestFailed: true,
            readabilityFailed: true,
            allowHarvestFailure,
            prerender,
          });
        }
      },
    );
  }

  async getReadability(
    cid: string,
    prerender: boolean,
    url: string,
  ): Promise<any> {
    const body = await this.getBody(cid, url, prerender);
    const mime = 'text/html';

    const dom = new JSDOM(body as any);
    const parser = new Readability(dom.window.document);
    const readability = parser.parse();
    if (readability) {
      return { body, mime, readability };
    } else {
      return { body, mime };
    }
  }

  private async getBody(cid: string, url: string, prerender: boolean) {
    if (prerender) {
      return this.puppeteer.getMarkup(cid, url, 7000);
    } else {
      // todo mag move to service and bypass cookie
      const response: Response = await fetch(url);
      if (response.status === 200) {
        return response.text();
      }
      throw new Error(`Invalid status ${response.status} for ${url}`);
    }
  }
}
