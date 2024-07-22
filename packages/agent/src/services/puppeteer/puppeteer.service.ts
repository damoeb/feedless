import { Browser, Frame, HTTPResponse, Page, ScreenshotClip, ScreenshotOptions } from 'puppeteer';
import puppeteer from 'puppeteer-extra';
import { Injectable, Logger } from '@nestjs/common';
import { pick } from 'lodash';
import { VerboseConfigService } from '../common/verbose-config.service';
import {
  DomActionType,
  DomElement,
  DomElementByNameOrXPath,
  DomElementByXPath,
  FetchActionDebugResponseInput,
  FieldWrapper,
  HttpFetch,
  NetworkRequest,
  PuppeteerWaitUntil,
  ScrapeAction,
  ScrapeActionResponseInput,
  ScrapeEmit,
  ScrapeExtract,
  ScrapeExtractResponse,
  ScrapeExtractResponseInput,
  ScrapePrerender,
  ScrapeRequest,
  ScrapeResponseInput
} from '../../generated/graphql';

interface Viewport {
  width: number;
  height: number;
  isMobile: boolean;
  isLandscape?: boolean;
}

interface EvaluateResponse {
  markup: string;
  text: string;
  boundingBox: ScreenshotClip;
}

type LogAppender = (msg: string) => void;

// todo use blocklist to speed up https://github.com/jmdugan/blocklists/tree/master/corporations
@Injectable()
export class PuppeteerService {
  private readonly log = new Logger(PuppeteerService.name);
  private readonly isDebug: boolean;
  // private readonly imageQuality = 100;
  private readonly imageType = 'png';
  private readonly queue: {
    job: ScrapeRequest;
    queuedAt: number;
    resolve: (response: ScrapeResponseInput) => void;
    reject: (reason: string) => void;
  }[] = [];
  private readonly maxWorkers: number;
  private currentActiveWorkers = 0;

  private readonly prerenderTimeout: number;
  private readonly defaultViewport: Viewport = {
    width: 1024,
    height: 768,
    isMobile: false,
  };

  constructor(config: VerboseConfigService) {
    const isProd: boolean = config.get('NODE_ENV')?.startsWith('prod');
    this.isDebug = config.getBoolean('DEBUG') && !isProd;
    this.maxWorkers = config.getInt('APP_MAX_WORKERS', { fallback: 5 });
    this.prerenderTimeout = config.getInt('APP_PRERENDER_TIMEOUT_MILLIS', {
      fallback: 40000,
    });

    const minTimout = 10000;
    if (this.prerenderTimeout < minTimout || isNaN(this.prerenderTimeout)) {
      this.log.log(`prerenderTimeout must be greater than ${minTimout}`);
      process.exit(1);
    }
  }

  public async submit(job: ScrapeRequest): Promise<ScrapeResponseInput> {
    return new Promise<ScrapeResponseInput>((resolve, reject) => {
      this.queue.push({ job, resolve, reject, queuedAt: Date.now() });
      if (this.currentActiveWorkers < this.maxWorkers) {
        this.startWorker(this.currentActiveWorkers).catch(reject);
      }
    }).catch((e) => {
      this.log.error(`[${job.corrId}] ${e}`);
      throw e;
    });
  }

  private async newBrowser(scrapeRequest: ScrapeRequest): Promise<Browser> {
    const viewport: Viewport = this.resolveViewport(scrapeRequest);
    console.log('viewport', viewport);
    return puppeteer.launch({
      headless: this.isDebug ? false : 'new',
      // devtools: false,
      // defaultViewport: scrapeRequest.page.viewport || {
      defaultViewport: pick(viewport, ['height', 'width']),
      executablePath: '/usr/bin/chromium-browser',
      timeout: getHttpGet(scrapeRequest).timeout || 30000,
      dumpio: this.isDebug,
      args: [
        `--window-size=${viewport.width},${viewport.height}`,
        '--disable-dev-shm-usage',
        // '--disable-background-networking',
        // Disable installation of default apps on first run
        '--disable-default-apps',
        // Disable all chrome extensions entirely
        // '--disable-extensions',
        // Disable the GPU hardware acceleration
        '--disable-gpu',
        // Disable syncing to a Google account
        '--disable-sync',
        // Disable built-in Google Translate service
        '--disable-translate',
        // Hide scrollbars on generated images/PDFs
        // '--hide-scrollbars',
        // Disable reporting to UMA, but allows for collection
        // '--metrics-recording-only',
        // Mute audio
        '--mute-audio',
        // Skip first run wizards
        '--no-first-run',
        // Disable sandbox mode
        // '--no-sandbox',
        // '--load-extension=ghostery-extension/extension-manifest-v2/dist',
        // Expose port 9222 for remote debugging
        //  '--remote-debugging-port=9222',
        // Disable fetching safebrowsing lists, likely redundant due to disable-background-networking
        '--safebrowsing-disable-auto-update',
      ],
    });
  }

  // http://localhost:3000/api/intern/prerender?url=https://derstandard.at

  private async executeRequest(
    request: ScrapeRequest,
    browser: Browser,
  ): Promise<ScrapeResponseInput> {
    const corrId = request.corrId;
    const startTime = Date.now();
    const page = await this.newPage(browser, request);
    const logs: string[] = [];
    let outputs: ScrapeActionResponseInput[];
    const appendLog: LogAppender = (msg: string) => {
      logs.push(msg);
      this.log.debug(msg);
    };
    this.interceptConsole(page, appendLog);

    try {
      const httpGet = getHttpGet(request);
      const timeout = httpGet.timeout || this.prerenderTimeout;
      appendLog(`timeout=${timeout}`);

      const headers = {};
      request.flow.sequence
        ?.filter((action) => !!action.header)
        .forEach((action) => {
          appendLog(`set header ${action.header.name}]`);
          headers[action.header.name] = action.header.value;
        });

      await page.setExtraHTTPHeaders(headers);

      appendLog(`executing ${request.flow.sequence.length} actions`);

      outputs = await request.flow.sequence.reduce(
        (waitFor, action) =>
          waitFor.then(async (outputs) => {
            const output = await this.executeAction(
              corrId,
              action,
              page,
              appendLog,
            );
            if (output) {
              outputs.push(output);
            }
            return outputs;
          }),
        Promise.resolve([] as ScrapeActionResponseInput[]),
      );
      appendLog(`all actions executed with ${outputs.length} outputs`);

      await this.waitForNetworkIdle(page, 1000);

      const { additionalWaitSec } = httpGet;
      if (additionalWaitSec > 0) {
        appendLog(`wait ${additionalWaitSec} sec`);
        await new Promise((resolve) =>
          setTimeout(resolve, additionalWaitSec * 1000),
        );
      }

      return {
        outputs,
        logs,
        failed: false,
      };
    } catch (e) {
      this.log.error(`[${corrId}] ${e.message}`);
      return {
        failed: true,
        logs,
        errorMessage: e.message,
        outputs: outputs || [],
      };
    }
  }

  private async waitForNetworkIdle(page: Page, timeout: number) {
    try {
      await page.waitForNetworkIdle({
        timeout,
      });
    } catch (e) {
      this.log.log(e.message);
    }
  }

  private async getDebug(
    corrId: string,
    url: string,
    logs: string[],
    networkDataHandle: () => Promise<NetworkRequest[]>,
    response: HTTPResponse,
    page: Page,
  ): Promise<FetchActionDebugResponseInput> {
    // const given = async (condition: boolean, label: string): Promise<void> => {
    //   if (condition) {
    //     this.log.log(`[${corrId}] appending ${label}`);
    //     return Promise.resolve();
    //   } else {
    //     return Promise.reject('');
    //   }
    // };

    const viewport = page.viewport();
    return {
      corrId,
      url,
      console: logs,
      network: await networkDataHandle(),
      statusCode: response?.status(),
      viewport: {
        width: viewport?.width || -1,
        height: viewport?.height || -1,
        isLandscape: viewport?.isLandscape || false,
        isMobile: viewport?.isMobile || false,
      },
      // metrics: {
      //   render: totalTimeUsed,
      //   queue: 0,
      // },
      contentType: response?.headers()['content-type'],
      // html: await page.evaluate(() => document.documentElement.outerHTML),
      cookies: await page
        .cookies()
        .then((cookies) => cookies.map((cookie) => JSON.stringify(cookie))),
      prerendered: true,
      screenshot: await this.extractScreenshot(page, undefined),
    };
  }

  private async grabElement(
    page: Page,
    fragmentName: string,
    xpath: string,
    exposePixel: boolean,
  ): Promise<ScrapeExtractResponse> {
    const evaluateResponse: EvaluateResponse = await page.evaluate(
      (baseXpath) => {
        let element: HTMLElement = document
          .evaluate(baseXpath.toString(), document, null, 5)
          .iterateNext() as HTMLElement;

        const isDocument = element?.nodeType === 9;
        if (isDocument) {
          element = (element as any).documentElement as HTMLElement;
        }
        const bb = element.getBoundingClientRect();
        const boundingBox = {
          x: bb.left,
          y: bb.top,
          width: bb.right - bb.left,
          height: bb.bottom - bb.top,
        };

        return {
          markup: element.outerHTML,
          text: element.outerText,
          boundingBox,
        };
      },
      xpath,
    );

    const getScreenshot = (): Promise<string> => {
      if (xpath === '/') {
        return page.screenshot({
          fullPage: true,
          optimizeForSpeed: false,
          // quality: this.imageQuality,
          type: this.imageType,
          encoding: 'base64',
        });
      } else {
        return this.extractScreenshot(
          page,
          this.extendBoundingBox(evaluateResponse.boundingBox, page),
        );
      }
    };

    return {
      fragmentName,
      fragments: [
        {
          html: {
            data: evaluateResponse.markup,
          },
          text: {
            data: evaluateResponse.text,
          },
          data: exposePixel
            ? {
              mimeType: 'image/png',
              data: await getScreenshot(),
            }
            : null,
        }
      ],
    };
  }
  private async grabBoundingBox(
    page: Page,
    fragmentName: string,
    boundingBox: { x: number; y: number; w: number; h: number },
  ): Promise<ScrapeExtractResponseInput> {
    const screenshot = await page.screenshot({
      clip: {
        x: boundingBox.x,
        y: boundingBox.y,
        height: boundingBox.h,
        width: boundingBox.w,
      },
      type: this.imageType,
      // quality: this.imageQuality,
      optimizeForSpeed: false,
      encoding: 'base64',
      captureBeyondViewport: true,
    });

    return {
      fragmentName,
      fragments: [
        {
          data:{
            mimeType: 'image/png',
            data: screenshot,
          }
        }
      ]
    };
  }

  private async newPage(browser: Browser, request: ScrapeRequest) {
    const page = await browser.newPage();
    await page.setCacheEnabled(false);
    await page.setBypassCSP(true);
    await page.setViewport(this.resolveViewport(request));
    // await page.setExtraHTTPHeaders(
    //   request.page.actions.filter(action => !isUndefined(action.header))
    //     .map(action => action.header)
    //     .reduce((headers, header) => {
    //       headers[header.name] = header.value;
    //       return headers;
    //     }, {})
    // );
    // todo cookies

    // if (process.env.USER_AGENT) {
    //   await page.setUserAgent(process.env.USER_AGENT);
    // }

    return page;
  }

  private async startWorker(workerId: number) {
    this.log.debug(`startWorker #${workerId}`);
    this.currentActiveWorkers++;
    while (this.queue.length > 0) {
      const { job, queuedAt, resolve, reject } = this.queue.shift();
      const httpGet = getHttpGet(job);
      this.log.debug(
        `worker #${workerId} consumes [${job.corrId}] ${httpGet.url}`,
      );

      const browser = await this.newBrowser(job);
      try {
        const response = await Promise.race([
          this.executeRequest(job, browser),
          new Promise<ScrapeResponseInput>((_, reject) =>
            setTimeout(
              () => reject(`timeout exceeded`),
              httpGet.timeout || this.prerenderTimeout,
            ),
          ),
        ]);
        if (!this.isDebug) {
          await browser.close();
        }
        const totalTime = Date.now() - queuedAt;
        this.log.log(`[${job.corrId}] prerendered within ${totalTime / 1000}s`);
        // const { metrics } = response.debug;
        // response.debug.metrics.queue = totalTime - metrics.render;

        resolve(response);
      } catch (e) {
        if (!this.isDebug) {
          await browser.close();
        }
        this.log.warn(
          `[${job.corrId}] prerendered within ${
            (Date.now() - queuedAt) / 1000
          }s ${e.message}`,
        );
        reject(e.message);
      }
    }
    this.currentActiveWorkers--;
    this.log.debug(`endWorker #${workerId}`);
  }

  private interceptNetwork(page: Page): () => Promise<NetworkRequest[]> {
    // page.on('request', request => {
    //   request_client({
    //     uri: request.url(),
    //     resolveWithFullResponse: true,
    //   }).then(response => {
    //     const request_url = request.url();
    //     const request_headers = request.headers();
    //     const request_post_data = request.postData();
    //     const response_headers = response.headers;
    //     const response_size = response_headers['content-length'];
    //     const response_body = response.body;
    //
    //     result.push({
    //       request_url,
    //       request_headers,
    //       request_post_data,
    //       response_headers,
    //       response_size,
    //       response_body,
    //     });
    //
    //     request.continue();
    //   });
    // });
    return async () => {
      await page.setRequestInterception(true);
      return [];
    };
  }

  private interceptConsole(page: Page, appendLog: LogAppender) {
    page.on('console', (consoleObj) => {
      appendLog(`[${consoleObj.type()}] ${consoleObj.text()}`);
    });
  }

  private async extractScreenshot(page: Page, boundingBox: ScreenshotClip) {
    const hasBoundingBox = !!boundingBox;
    const options: ScreenshotOptions = {};

    if (hasBoundingBox) {
      this.log.log(`screenshot ${JSON.stringify(boundingBox)}`);
      this.log.log(`viewport ${JSON.stringify(page.viewport())}`);
      options.clip = boundingBox;
    } else {
      this.log.log(`screenshot full page`);
      options.fullPage = true;
    }

    const screenshot = await page.screenshot({
      ...options,
      type: this.imageType,
      // quality: this.imageQuality,
      optimizeForSpeed: false,
      encoding: 'base64',
      captureBeyondViewport: true,
    });
    console.assert(screenshot.length > 0, 'screenshot is empty');
    return screenshot;
  }

  private extendBoundingBox(bb: ScreenshotClip, page: Page): ScreenshotClip {
    const margin = 5;
    return {
      x: Math.max(0, bb.x - margin),
      y: Math.max(0, bb.y - margin),
      width: Math.min(page.viewport().width, bb.width + 2 * margin),
      height: Math.min(page.viewport().height, bb.height + 2 * margin),
      scale: bb.scale,
    };
  }

  private async executeAction(
    corrId: string,
    action: FieldWrapper<ScrapeAction>,
    page: Page,
    appendLog: LogAppender,
  ): Promise<ScrapeActionResponseInput | void> {
    const appender = (msg: string) => appendLog(`action ${msg}`);
    // if (action.header) {
    //   return this.executeHeaderAction(action.header, page, appender);
    // }
    if (action.click) {
      return this.executeClickAction(action.click, page, appender);
    }
    if (action.fetch) {
      return this.executeFetchAction(corrId, action.fetch, page, appender);
    }
    if (action.wait) {
      return this.executeWaitAction(action.wait, page, appender);
    }
    if (action.type) {
      return this.executeTypeAction(action.type, page, appender);
    }
    if (action.purge) {
      return this.executePurgeAction(action.purge, page, appender);
    }
    if (action.extract) {
      return this.extractAction(corrId, action.extract, page, appender);
    }
  }

  private async executeWaitAction(
    element: DomElement,
    page: Page,
    appendLog: LogAppender,
  ) {
    appendLog(`wait for ${JSON.stringify(element.element)}`);
    await page.waitForSelector(this.resolveSelector(element.element));
  }

  private async extractAction(
    corrId: String,
    extract: ScrapeExtract,
    page: Page,
    appendLog: (msg: string) => void,
  ): Promise<ScrapeActionResponseInput> {
    const fragmentName = extract.fragmentName;
    if (extract.selectorBased) {
      const xpath = extract.selectorBased.xpath.value;
      const exposePixel = extract.selectorBased.emit.includes(ScrapeEmit.Pixel);
      appendLog(
        `extract fragment '${fragmentName}' xpath ${xpath} pixel=${exposePixel}`,
      );
      return {
        extract: await this.grabElement(page, fragmentName, xpath, exposePixel),
      };
    } else {
      if (extract.imageBased.boundingBox) {
        appendLog(
          `extract fragment '${fragmentName}' bbox ${extract.imageBased.boundingBox}`,
        );
        return {
          extract: await this.grabBoundingBox(
            page,
            fragmentName,
            extract.imageBased.boundingBox,
          ),
        };
      } else {
        throw new Error(`[${corrId}] Underspecified fragment`);
      }
    }
  }

  private async executeTypeAction(
    element: DomActionType,
    page: Page,
    appendLog: LogAppender,
  ) {
    appendLog(
      `type '${element.typeValue}' in ${JSON.stringify(element.element)}`,
    );
    await page.type(
      this.resolveXpathSelector(element.element),
      element.typeValue,
    );
  }

  private async executePurgeAction(
    element: DomElementByXPath,
    page: Page,
    appendLog: LogAppender,
  ) {
    appendLog(`purge '${element.value}'`);
    await page.evaluate((selector) => {
      Array.from(document.querySelectorAll(selector)).forEach((el) =>
        el.remove(),
      );
    }, this.resolveXpathSelector(element));
  }

  private async executeClickAction(
    element: DomElement,
    page: Page,
    appendLog: LogAppender,
  ) {
    appendLog(`click ${JSON.stringify(element)}`);
    if (element.position) {
      const pos = element.position;
      await page.mouse.click(pos.x, pos.y);
    } else {
      let selector: string;
      let frameOrPage: Frame | Page;

      if (element.element) {
        selector = this.resolveSelector(element.element);
        frameOrPage = page;
      } else {
        throw new Error('Unsupported click action');
      }

      appendLog(`resolve element ${selector}`);
      await frameOrPage.waitForSelector(selector);
      await frameOrPage.$eval(selector, (element) => element.scrollIntoView());
      await frameOrPage.click(selector);
    }

    await this.waitForNetworkIdle(page, 1000);
  }

  private resolveSelector(element: DomElementByNameOrXPath) {
    if (element.name) {
      return `::-p-text(${element.name.value})`;
    } else {
      return this.resolveXpathSelector(element.xpath);
    }
  }

  private resolveXpathSelector(element: DomElementByXPath) {
    return `::-p-xpath(${element.value})`;
  }

  private resolveViewport(request: ScrapeRequest) {
    if (getHttpGet(request).viewport) {
      return pick(getHttpGet(request).viewport, [
        'height',
        'isLandscape',
        'isMobile',
        'width',
      ]);
    } else {
      return this.defaultViewport;
    }
  }

  private async executeFetchAction(
    corrId: string,
    httpGet: HttpFetch,
    page: Page,
    appendLog: (msg: string) => void,
  ): Promise<ScrapeActionResponseInput> {
    const networkDataHandle = this.interceptNetwork(page);
    const url = httpGet.get.url.literal;
    appendLog(`fetch url ${url}`);
    const response = await page.goto(url, {
      waitUntil: httpGet.get.waitUntil || PuppeteerWaitUntil.Load,
      timeout: httpGet.get.timeout,
    });

    const logs: string[] = [];
    page.on('console', (consoleObj) => {
      const message = `[browser] ${consoleObj?.text()}`;
      logs.push(message);
      appendLog(message);
    });
    await this.waitForNetworkIdle(page, 1000);
    return {
      fetch: {
        data: await page.evaluate(() => document.documentElement.outerHTML),
        debug: await this.getDebug(
          corrId,
          url,
          logs,
          networkDataHandle,
          response,
          page,
        ),
      },
    };
  }
}

export function getHttpGet(scrapeRequest: ScrapeRequest): ScrapePrerender {
  return scrapeRequest.flow.sequence.find((a) => a.fetch).fetch.get;
}
