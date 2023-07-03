import { Browser, Page, ScreenshotClip } from 'puppeteer';
import puppeteer from 'puppeteer-extra';
import { Injectable, Logger } from '@nestjs/common';
import {
  HarvestEmitType,
  ScrapedElement,
  ScrapeElement,
  ScrapeRequest,
  ScrapeResponse,
  NetworkRequest
} from 'client-lib';

// todo use blocklist to speed up https://github.com/jmdugan/blocklists/tree/master/corporations
@Injectable()
export class PuppeteerService {
  private readonly log = new Logger(PuppeteerService.name);
  private readonly isDebug: boolean;
  private readonly queue: {
    job: ScrapeRequest;
    queuedAt: number;
    resolve: (response: ScrapeResponse) => void;
    reject: (reason: string) => void;
  }[] = [];
  private readonly maxWorkers = process.env.APP_MAX_WORKERS || 5;
  private currentActiveWorkers = 0;

  private readonly prerenderTimeout: number = 20000;
  private readonly execEvalScriptTimeout: number = 10000;

  constructor() {
    const isProd: boolean = process.env.NODE_ENV === 'prod';
    this.isDebug = process.env.DEBUG === 'true' && !isProd;
    this.log.log(`maxWorkers=${this.maxWorkers}`);
    this.log.log(
      `debug=${this.isDebug} (to activate set process.env.DEBUG=true)`,
    );
    if (isProd) {
      this.prerenderTimeout = parseInt(
        process.env.APP_PRERENDER_TIMEOUT_MILLIS,
        10,
      );
      this.execEvalScriptTimeout = parseInt(
        process.env.APP_PRERENDER_EVAL_SCRIPT_TIMEOUT_MILLIS,
        10,
      );
    }

    this.log.log(`prerenderTimeout=${this.prerenderTimeout}`);
    this.log.log(`execEvalScriptTimeout=${this.execEvalScriptTimeout}`);
    const minTimout = 2000;
    if (this.prerenderTimeout < minTimout || isNaN(this.prerenderTimeout)) {
      this.log.log(`prerenderTimeout must be greater than ${minTimout}`);
      process.exit(1);
    }
    if (
      this.execEvalScriptTimeout < minTimout ||
      isNaN(this.execEvalScriptTimeout)
    ) {
      this.log.log(`execEvalScriptTimeout must be greater than ${minTimout}`);
      process.exit(1);
    }
  }

  public async submit(job: ScrapeRequest): Promise<ScrapeResponse> {
    return new Promise<ScrapeResponse>((resolve, reject) => {
      this.queue.push({ job, resolve, reject, queuedAt: Date.now() });
      if (this.currentActiveWorkers < this.maxWorkers) {
        this.startWorker(this.currentActiveWorkers).catch(reject);
      }
    }).catch((e) => {
      this.log.error(`[${job.corrId}] ${e}`);
      return {
        id: job.id,
        corrId: job.corrId,
        error: e?.message,
        url: null,
      };
    });
  }

  private async newBrowser(job: ScrapeRequest): Promise<Browser> {
    return puppeteer.launch({
      headless: 'new',
      // devtools: false,
      // defaultViewport: job.page.viewport || {
      defaultViewport: {
        width: 1024,
        height: 768,
      },
      executablePath: '/usr/bin/chromium-browser',
      timeout: job.page.timeout || 30000,
      dumpio: this.isDebug,
      args: [
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
        '--load-extension=ghostery-extension/extension-manifest-v2/dist',
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
  ): Promise<ScrapeResponse> {
    const corrId = request.corrId;

    const page = await this.newPage(browser);
    try {
      await page.goto(request.page.url, {
        waitUntil: request.page.waitUntil,
        timeout: request.page.timeout || this.prerenderTimeout,
      });

      const networkDataHandle = this.interceptNetwork(page, request);
      const consoleDataHandle = this.interceptConsole(page, request);

      if (request.page.evalScript) {
        page.on('console', (consoleObj) =>
          this.log.debug(`[${corrId}][chrome] ${consoleObj?.text()}`),
        );
        this.log.log(
          `[${corrId}] evaluating evalScript '${request.page.evalScript}'`,
        );
        await Promise.race([
          new Promise((resolve, reject) => {
            setTimeout(
              reject,
              request.page.evalScriptTimeout || this.execEvalScriptTimeout,
            );
          }),
          page.evaluate(request.page.evalScript),
        ]);
      }

      return {
        elements: await Promise.all(
          request.elements.map((element) => this.grabElement(page, element)),
        ),
        url: page.url(),
        debug: {
          console: await consoleDataHandle(),
          network: await networkDataHandle(),
          html: request.debug?.html ? await page.evaluate(() => document.documentElement.innerHTML) : undefined,
          // cookies: request.debug?.cookies ? await page.cookies().then(cookies => cookies.map(cookie => cookie.toString())) : undefined,
          screenshot: request.debug?.screenshot ? await page.screenshot({fullPage: true, encoding: 'base64'}) : undefined
        }
      };
    } catch (e) {
      this.log.error(`[${corrId}] ${e.message}`);
      return {
        error: e.message,
        url: page.url(),
      };
    }
  }

  private async grabElement(
    page: Page,
    element: ScrapeElement,
  ): Promise<ScrapedElement> {
    const emitMarkup = element.emit === HarvestEmitType.Markup;
    const emitText = element.emit === HarvestEmitType.Text;
    const emitPixel = element.emit === HarvestEmitType.Pixel;
    const response: string | undefined | ScreenshotClip = await page.evaluate(
      (baseXpath, emitMarkup, emitText, emitBoundingBox) => {
        let element: HTMLElement = document
          .evaluate(baseXpath.toString(), document, null, 5)
          .iterateNext() as HTMLElement;

        const isDocument = element.nodeType === 9;
        if (isDocument) {
          element = (element as any).documentElement as HTMLElement;
        }
        if (emitMarkup) {
          console.log('markup');
          return element.outerHTML;
        }
        if (emitText) {
          console.log('text');
          return element.outerText;
        }
        if (emitBoundingBox) {
          const bb = element.getBoundingClientRect();
          console.log('pixel');
          return {
            x: bb.left,
            y: bb.top,
            width: bb.right - bb.left,
            height: bb.bottom - bb.top,
          };
        }
      },
      element.xpath,
      emitMarkup,
      emitText,
      emitPixel,
    );

    if (emitMarkup || emitText) {
      return { dataAscii: response as any, xpath: element.xpath };
    }
    this.log.log(`screenshot ${JSON.stringify(response)}`)
    const screenshot = await page.screenshot({
      clip: response as ScreenshotClip,
    });
    return {
      dataBase64: screenshot.toString('base64'),
      xpath: element.xpath,
    };
  }

  private async newPage(browser: Browser) {
    const page = await browser.newPage();
    await page.setCacheEnabled(false);
    await page.setBypassCSP(true);
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
          new Promise<ScrapeResponse>((_, reject) =>
            setTimeout(
              () => reject(`timeout exceeded`),
              this.prerenderTimeout + this.execEvalScriptTimeout,
            ),
          ),
        ]);
        if (!this.isDebug) {
          await browser.close();
        }
        this.log.log(
          `[${job.corrId}] prerendered within ${
            (Date.now() - queuedAt) / 1000
          }s`,
        );
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

  private interceptNetwork(page: Page, request: ScrapeRequest): () => Promise<NetworkRequest[]> {
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
      return []
    }
  }

  private interceptConsole(page: Page, request: ScrapeRequest): () => Promise<string[]> {
    const logs: string[] = [];
    const corrId = request.corrId;
    page.on('console', (consoleObj) => {
        this.log.debug(`[${corrId}][chrome] ${consoleObj?.text()}`);
        logs.push(`[${corrId}] [${consoleObj.type()}] ${consoleObj.text()}`)
      });

    return () => Promise.resolve(logs);
  }
}
