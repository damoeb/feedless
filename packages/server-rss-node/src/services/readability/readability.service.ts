import { Injectable } from '@nestjs/common';
import { Readability } from 'mozilla-readability';
import { JSDOM } from 'jsdom';
import fetch, { Response } from 'node-fetch';

@Injectable()
export class ReadabilityService {
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
