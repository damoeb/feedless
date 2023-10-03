import { Browser, Frame, HTTPResponse, Page, ScreenshotClip } from 'puppeteer';
import puppeteer from 'puppeteer-extra';
import { Injectable, Logger } from '@nestjs/common';
import {
  NetworkRequest,
  PuppeteerWaitUntil,
  ScrapeEmitType,
  ScrapeRequest,
} from 'client-lib';
import { pick } from 'lodash';
import {
  DomActionType,
  DomElementByNameOrXPath,
  DomElementByXPath,
  EmittedScrapeData,
  FieldWrapper,
  ScrapeAction,
  ScrapeDebugResponseInput,
  ScrapedElementInput,
  ScrapeResponseInput,
} from 'client-lib/dist/generated/graphql';
import { VerboseConfigService } from '../common/verbose-config.service';
import { DomElement } from 'client-lib/src/generated/graphql';

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
      fallback: 20000,
    });

    const minTimout = 2000;
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

  private async newBrowser(job: ScrapeRequest): Promise<Browser> {
    const viewport: Viewport =
      job.page.prerender?.viewport || this.defaultViewport;
    return puppeteer.launch({
      headless: this.isDebug ? false : 'new',
      // devtools: false,
      // defaultViewport: job.page.viewport || {
      defaultViewport: pick(viewport, ['height', 'width']),
      executablePath: '/usr/bin/chromium-browser',
      timeout: job.page.timeout || 30000,
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

  private async request(
    request: ScrapeRequest,
    browser: Browser,
  ): Promise<ScrapeResponseInput> {
    const corrId = request.corrId;
    const startTime = Date.now();
    const page = await this.newPage(browser, request);
    const networkDataHandle = this.interceptNetwork(page, request);
    const logs: string[] = [];
    const appendLog: LogAppender = (msg: string) => {
      logs.push(msg);
      this.log.log(msg);
    };
    this.interceptConsole(page, request, appendLog);

    let response: HTTPResponse = null;
    try {
      const prerender = request.page.prerender;
      const timeout = request.page.timeout || this.prerenderTimeout;
      appendLog(`timeout=${timeout}`);
      response = await page.goto(request.page.url, {
        waitUntil: prerender?.waitUntil || PuppeteerWaitUntil.Load,
        timeout,
      });

      page.on('console', (consoleObj) =>
        this.log.debug(`[${corrId}][chrome] ${consoleObj?.text()}`),
      );
      try {
        await page.waitForNetworkIdle({
          timeout: 1000,
        });
      } catch (e) {
        this.log.warn(e.message);
      }

      await request.page.actions?.reduce(
        (waitFor, action) =>
          waitFor.then(() => this.executeAction(action, page, appendLog)),
        Promise.resolve(),
      );

      try {
        await page.waitForNetworkIdle({
          timeout: 1000,
        });
      } catch (e) {
        this.log.warn(e.message);
      }

      return {
        elements: await Promise.all(
          (request.elements || ['/']).map((xpath) =>
            this.grabElement(page, xpath, request.emit),
          ),
        ),
        url: response.url(),
        failed: false,
        debug: await this.getDebug(
          request.corrId,
          logs,
          networkDataHandle,
          request,
          response,
          page,
          Date.now() - startTime,
        ),
      };
    } catch (e) {
      this.log.error(`[${corrId}] ${e.message}`);
      return {
        failed: true,
        errorMessage: e.message,
        url: response?.url() || page.url(),
        elements: [],
        debug: await this.getDebug(
          request.corrId,
          logs,
          networkDataHandle,
          request,
          response,
          page,
          Date.now() - startTime,
        ),
      };
    }
  }

  private async getDebug(
    corrId: string,
    logs: string[],
    networkDataHandle: () => Promise<NetworkRequest[]>,
    request: ScrapeRequest,
    response: HTTPResponse,
    page: Page,
    totalTimeUsed: number,
  ): Promise<ScrapeDebugResponseInput> {
    return {
      corrId,
      console: logs,
      network: await networkDataHandle(),
      statusCode: response?.status(),
      metrics: {
        render: totalTimeUsed,
        queue: 0,
      },
      contentType: response?.headers()['content-type'],
      viewport: this.resolveViewport(request),
      html: request.debug?.html
        ? await page.evaluate(() => document.documentElement.outerHTML)
        : undefined,
      cookies: request.debug?.cookies
        ? await page
            .cookies()
            .then((cookies) => cookies.map((cookie) => cookie.toString()))
        : [],
      prerendered: true,
      screenshot: request.debug?.screenshot
        ? await page.screenshot({ fullPage: true, encoding: 'base64' })
        : undefined,
    };
  }

  private async grabElement(
    page: Page,
    xpath: string,
    emit: ScrapeEmitType[],
  ): Promise<ScrapedElementInput> {
    const response: EvaluateResponse = await page.evaluate((baseXpath) => {
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
    }, xpath);

    const shouldEmit = (oneOfTypes: ScrapeEmitType[]): boolean => {
      return oneOfTypes.some((type) => emit.includes(type));
    };

    const scrapeData: EmittedScrapeData[] = [];

    if (
      shouldEmit([
        ScrapeEmitType.Markup,
        ScrapeEmitType.Feeds,
        ScrapeEmitType.Readability,
      ])
    ) {
      scrapeData.push({
        type: ScrapeEmitType.Markup,
        markup: response.markup,
      });
    }
    if (shouldEmit([ScrapeEmitType.Text])) {
      scrapeData.push({
        type: ScrapeEmitType.Text,
        text: response.text,
      });
    }
    if (shouldEmit([ScrapeEmitType.Pixel])) {
      scrapeData.push({
        type: ScrapeEmitType.Pixel,
        pixel: await this.extractScreenshot(
          page,
          this.extendBoundingBox(response.boundingBox, page),
        ),
      });
    }

    return {
      xpath: xpath,
      data: scrapeData,
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
      this.log.debug(
        `worker #${workerId} consumes [${job.corrId}] ${job.page.url}`,
      );

      const browser = await this.newBrowser(job);
      try {
        const response = await Promise.race([
          this.request(job, browser),
          new Promise<ScrapeResponseInput>((_, reject) =>
            setTimeout(
              () => reject(`timeout exceeded`),
              job.page.timeout || this.prerenderTimeout,
            ),
          ),
        ]);
        if (!this.isDebug) {
          await browser.close();
        }
        const totalTime = Date.now() - queuedAt;
        this.log.log(`[${job.corrId}] prerendered within ${totalTime / 1000}s`);
        const { metrics } = response.debug;
        response.debug.metrics.queue = totalTime - metrics.render;

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

  private interceptNetwork(
    page: Page,
    request: ScrapeRequest,
  ): () => Promise<NetworkRequest[]> {
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

  private interceptConsole(
    page: Page,
    request: ScrapeRequest,
    appendLog: LogAppender,
  ) {
    const corrId = request.corrId;
    page.on('console', (consoleObj) => {
      this.log.debug(`[${corrId}][chrome] ${consoleObj?.text()}`);
      appendLog(`[${consoleObj.type()}] ${consoleObj.text()}`);
    });
  }

  private async extractScreenshot(page: Page, boundingBox: ScreenshotClip) {
    this.log.log(`screenshot ${JSON.stringify(boundingBox)}`);
    this.log.log(`viewport ${JSON.stringify(page.viewport())}`);
    const screenshot = await page.screenshot({
      clip: boundingBox,
    });
    return screenshot.toString('base64');
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
    action: FieldWrapper<ScrapeAction>,
    page: Page,
    appendLog: LogAppender,
  ) {
    if (action.click) {
      await this.executeClickAction(action.click, page, appendLog);
    }
    if (action.wait) {
      await this.executeWaitAction(action.wait, page, appendLog);
    }
    if (action.type) {
      await this.executeTypeAction(action.type, page, appendLog);
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

      const iframeRef = element.iframe;
      if (iframeRef) {
        appendLog(`resolve iframe ${iframeRef.xpath.value}`);
        await page.waitForXPath(iframeRef.xpath.value, { visible: true });
        const elementHandle = await page.$(
          `::-p-xpath(${iframeRef.xpath.value})`,
        );
        const frame = await elementHandle.contentFrame();
        selector = this.resolveSelector(iframeRef.nestedElement);
        frameOrPage = frame;
      } else if (element.element) {
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
    await page.waitForNavigation({
      waitUntil: 'networkidle2',
    });
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
    return (
      pick(request.page.prerender?.viewport, [
        'height',
        'isLandscape',
        'isMobile',
        'width',
      ]) || this.defaultViewport
    );
  }
}
