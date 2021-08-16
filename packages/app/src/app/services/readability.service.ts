import { Injectable } from '@angular/core';
import * as Readability from '@mozilla/readability/Readability';
import { HttpProxy } from './http-proxy.service';

@Injectable({
  providedIn: 'root',
})
export class ReadabilityService {
  constructor(private httpProxy: HttpProxy) {}

  public get(url: string): Promise<Readability> {
    return this.httpProxy.get(url).then(async (html) => {
      return this.getReadability(html, url);
    });
  }

  private createReadabilityParser(
    iframe: HTMLIFrameElement,
    timeout: number,
    resolve: (data: Readability) => void,
    reject: (error: Error) => void
  ) {
    let timeoutId;
    const parser = () => {
      clearTimeout(timeoutId);
      try {
        const readability = new Readability(iframe.contentDocument);
        resolve(readability.parse());
      } catch (e) {
        console.error(e);
        reject(e);
      }
    };
    timeoutId = setTimeout(parser, timeout);
    return parser;
  }

  private getReadability(
    staticHtml: string,
    url: string
  ): Promise<Readability> {
    console.log('Extracting readability');

    const iframe = document.createElement('iframe');
    return new Promise<any>((resolve, reject) => {
      const html = staticHtml.replace(
        /<\/head>/i,
        `<base href="${url}"></head>`
      );
      const blob = new Blob([html], { type: 'text/html' });
      iframe.src = window.URL.createObjectURL(blob);
      iframe.onload = this.createReadabilityParser(
        iframe,
        10000,
        resolve,
        reject
      );

      document.body.appendChild(iframe);
    })
      .catch(() => {
        return ReadabilityService.getReadabilityStatic(staticHtml);
      })
      .finally(() => {
        document.body.removeChild(iframe);
      });
  }

  private static getReadabilityStatic(staticHtml: string): Readability {
    const dom = new DOMParser().parseFromString(staticHtml, 'text/xml');
    const readability = new Readability(dom);
    return readability.parse();
  }
}
