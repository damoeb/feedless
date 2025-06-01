import puppeteer, {
  Browser,
  Frame,
  HTTPResponse,
  Page,
  ScreenshotClip,
  ScreenshotOptions,
  Viewport,
} from 'puppeteer';
import process from 'node:process';
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
  HttpGetRequest,
  LogStatementInput,
  NetworkRequest,
  PuppeteerWaitUntil,
  ScrapeAction,
  ScrapeActionResponseInput,
  ScrapeEmit,
  ScrapeExtract,
  ScrapeExtractResponseInput,
  ScrapeOutputResponseInput,
  ScrapeResponseInput,
  Source,
} from '../../generated/graphql';

interface EvaluateResponse {
  markup: string;
  text: string;
  boundingBox: ScreenshotClip;
}

// credits https://stackoverflow.com/a/38340730
function removeEmpty(obj) {
  return Object.fromEntries(
    Object.entries(obj)
      .filter(([_, v]) => v != null)
      .map(([k, v]) => [k, v === Object(v) ? removeEmpty(v) : v]),
  );
}

function stringifyJson(obj: object) {
  return JSON.stringify(removeEmpty(obj), null, 2);
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
    job: Source;
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

  constructor(private readonly config: VerboseConfigService) {
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

  public async submit(job: Source): Promise<ScrapeResponseInput> {
    return new Promise<ScrapeResponseInput>((resolve, reject) => {
      this.queue.push({ job, resolve, reject, queuedAt: Date.now() });
      if (this.currentActiveWorkers < this.maxWorkers) {
        this.startWorker(this.currentActiveWorkers).catch(reject);
      }
    });
  }

  async newBrowser(source: Source): Promise<Browser> {
    const viewport: Viewport = this.resolveViewport(source);
    return puppeteer.launch({
      headless: this.isDebug ? false : 'shell',
      devtools: false,
      defaultViewport: viewport,
      executablePath: this.config.getString('APP_CHROMIUM_BIN', {
        fallback: '/usr/bin/chromium-browser',
      }),
      timeout: getHttpGet(source).timeout || 30000,
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
    request: Source,
    browser: Browser,
  ): Promise<ScrapeResponseInput> {
    const logs: LogStatementInput[] = [];
    const appendLog: LogAppender = (msg: string) => {
      logs.push({
        time: new Date().getTime(),
        message: msg,
      });
      this.log.log(msg);
    };
    appendLog(`Starting job id=${request.id}`);

    const page = await this.newPage(browser, request);
    const outputs: ScrapeOutputResponseInput[] = [];
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

      appendLog(`executing ${stringifyJson(request.flow.sequence)}`);

      const createLogAppender = (prefix: string) => (msg: string) =>
        appendLog(`${prefix} ${msg}`);

      await request.flow.sequence.reduce(
        (waitFor, action, currentIndex) =>
          waitFor.then(async () => {
            const writeLog = createLogAppender(`action#${currentIndex}`);
            const output = await this.executeAction(action, page, writeLog);
            if (output) {
              writeLog(
                `terminated with output of type ${Object.keys(output).find(
                  (key) => !!output[key],
                )}`,
              );
              outputs.push({
                index: currentIndex,
                response: output,
              });
            } else {
              writeLog(`terminated without output`);
            }
          }),
        Promise.resolve(),
      );
      appendLog(`all actions executed with ${outputs.length} outputs`);

      await this.waitForNetworkIdle(page, 1000);

      const { additionalWaitSec } = httpGet;
      if (additionalWaitSec > 0) {
        appendLog(`wait ${additionalWaitSec} sec`);
        await new Promise((resolve) =>
          setTimeout(resolve, additionalWaitSec * 1000),
        );
        appendLog(`waiting done`);
      }

      return {
        outputs,
        logs,
        ok: true,
      };
    } catch (e) {
      appendLog(e.message);
      this.log.error(e.message, e);
      return {
        ok: false,
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
      corrId: '',
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
        .then((cookies) => cookies.map((cookie) => stringifyJson(cookie))),
      prerendered: true,
      screenshot: await this.extractScreenshot(page, undefined),
    };
  }

  private async grabElement(
    page: Page,
    fragmentName: string,
    xpath: string,
    exposePixel: boolean,
  ): Promise<ScrapeExtractResponseInput> {
    this.log.log(
      `grabElement fragmentName=${fragmentName} xpath=${xpath} exposePixel=${exposePixel}`,
    );
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
        },
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
          data: {
            mimeType: 'image/png',
            data: screenshot,
          },
        },
      ],
    };
  }

  async newPage(browser: Browser, request: Source) {
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
      this.log.debug(`worker #${workerId} consumes ${httpGet.url}`);

      const browser = await this.newBrowser(job);
      try {
        const response = await Promise.race([
          this.executeRequest(job, browser),
          new Promise<ScrapeResponseInput>((_, reject) =>
            setTimeout(
              () => reject(new Error(`timeout exceeded`)),
              httpGet.timeout || this.prerenderTimeout,
            ),
          ),
        ]);
        if (!this.isDebug) {
          await browser.close();
        }
        const totalTime = Date.now() - queuedAt;
        this.log.log(`prerendered within ${totalTime / 1000}s`);
        // const { metrics } = response.debug;
        // response.debug.metrics.queue = totalTime - metrics.render;

        resolve(response);
      } catch (e) {
        if (!this.isDebug) {
          await browser.close();
        }
        this.log.warn(
          `prerendered within ${(Date.now() - queuedAt) / 1000}s ${e.message}`,
          e,
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
      appendLog(`[browser] ${consoleObj.text()}`);
    });
  }

  private async extractScreenshot(page: Page, boundingBox: ScreenshotClip) {
    const hasBoundingBox = !!boundingBox;
    const options: ScreenshotOptions = {};

    if (hasBoundingBox) {
      this.log.log(`screenshot ${stringifyJson(boundingBox)}`);
      this.log.log(`viewport ${stringifyJson(page.viewport())}`);
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
    action: FieldWrapper<ScrapeAction>,
    page: Page,
    appendLog: LogAppender,
  ): Promise<ScrapeActionResponseInput | void> {
    const createLogger = (prefix: string) => (msg: string) =>
      appendLog(`${prefix} ${msg}`);
    // if (action.header) {
    //   return this.executeHeaderAction(action.header, page, appender);
    // }
    if (action.click) {
      return this.executeClickAction(action.click, page, createLogger('click'));
    }
    if (action.fetch) {
      return this.executeFetchAction(action.fetch, page, createLogger('fetch'));
    }
    if (action.waitFor) {
      return this.executeWaitAction(action.waitFor, page, createLogger('wait'));
    }
    if (action.type) {
      return this.executeTypeAction(action.type, page, createLogger('type'));
    }
    if (action.purge) {
      return this.executePurgeAction(action.purge, page, createLogger('purge'));
    }
    if (action.extract) {
      return this.extractAction(action.extract, page, createLogger('extract'));
    }
    appendLog(`ignoring action ${stringifyJson(action)}`);
  }

  private async executeWaitAction(
    element: DomElement,
    page: Page,
    appendLog: LogAppender,
  ) {
    appendLog(`wait for ${stringifyJson(element.element)}`);
    await page.waitForSelector(this.resolveSelector(element.element));
  }

  private async extractAction(
    extract: ScrapeExtract,
    page: Page,
    appendLog: (msg: string) => void,
  ): Promise<ScrapeActionResponseInput> {
    const fragmentName = extract.fragmentName;
    if (extract.selectorBased) {
      const xpath = extract.selectorBased.xpath.value;
      const exposePixel = extract.selectorBased.emit.includes(ScrapeEmit.Pixel);
      appendLog(
        `fragment='${fragmentName}' xpath='${xpath}' pixel=${exposePixel}`,
      );
      return {
        extract: await this.grabElement(page, fragmentName, xpath, exposePixel),
      };
    } else {
      if (extract.imageBased.boundingBox) {
        appendLog(
          `fragment='${fragmentName}' bbox=${extract.imageBased.boundingBox}`,
        );
        return {
          extract: await this.grabBoundingBox(
            page,
            fragmentName,
            extract.imageBased.boundingBox,
          ),
        };
      } else {
        throw new Error(`Underspecified fragment`);
      }
    }
  }

  private async executeTypeAction(
    element: DomActionType,
    page: Page,
    appendLog: LogAppender,
  ) {
    appendLog(
      `type '${element.typeValue}' in ${stringifyJson(element.element)}`,
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
    if (element.position) {
      appendLog(`position-click ${stringifyJson(element)}`);
      const pos = element.position;
      await page.mouse.click(pos.x, pos.y);
    } else {
      let selector: string;
      let frameOrPage: Frame | Page;

      if (element.element) {
        appendLog(`element-click ${stringifyJson(element.element)}`);
        selector = this.resolveSelector(element.element);
        appendLog(`selector ${selector}`);
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

  private resolveViewport(request: Source): Viewport {
    if (getHttpGet(request).viewport) {
      return pick(getHttpGet(request).viewport, [
        'height',
        'isLandscape',
        'isMobile',
        'width',
      ]) as Viewport;
    } else {
      return this.defaultViewport;
    }
  }

  private async executeFetchAction(
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
      const message = `[browser-console] ${consoleObj?.text()}`;
      logs.push(message);
      appendLog(message);
    });
    await this.waitForNetworkIdle(page, 1000);
    return {
      fetch: {
        data: await page.evaluate(() => document.documentElement.outerHTML),
        debug: await this.getDebug(
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

export function getHttpGet(source: Source): HttpGetRequest {
  return source.flow.sequence.find((a) => a.fetch).fetch.get;
}
