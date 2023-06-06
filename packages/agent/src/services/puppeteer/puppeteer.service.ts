import { Browser, Page, ScreenshotClip } from 'puppeteer';
import puppeteer from 'puppeteer-extra';
import { Injectable, Logger } from '@nestjs/common';
import {
  HarvestEmitType,
  ScrapedElement,
  ScrapeElement,
  ScrapeRequest,
  ScrapeResponse,
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

  private readonly prerenderTimeout: number = 10000;
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

  private static launchLocal(debug: boolean) {
    return puppeteer.launch({
      headless: 'new',
      // devtools: false,
      defaultViewport: {
        width: 1024,
        height: 768,
      },
      executablePath: '/usr/bin/chromium-browser',
      timeout: 10000,
      dumpio: debug,
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

  private async newBrowser(): Promise<Browser> {
    return PuppeteerService.launchLocal(this.isDebug);
  }

  // http://localhost:3000/api/intern/prerender?url=https://derstandard.at

  private async request(
    req: ScrapeRequest,
    browser: Browser,
  ): Promise<ScrapeResponse> {
    const corrId = req.corrId;

    const page = await this.newPage(browser);
    try {
      await page.goto(req.page.url, {
        waitUntil: req.page.waitUntil,
        timeout: this.prerenderTimeout,
      });

      if (req.page.evalScript) {
        page.on('console', (consoleObj) =>
          this.log.debug(`[${corrId}][chrome] ${consoleObj?.text()}`),
        );
        this.log.log(
          `[${corrId}] evaluating evalScript '${req.page.evalScript}'`,
        );
        await Promise.race([
          new Promise((resolve, reject) => {
            setTimeout(
              reject,
              req.page.evalScriptTimeout || this.execEvalScriptTimeout,
            );
          }),
          page.evaluate(req.page.evalScript),
        ]);
      }

      return {
        elements: await Promise.all(
          req.elements.map((element) => this.grabElement(page, element)),
        ),
        url: page.url(),
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
          console.log('pixel');
          return {
            x: element.clientLeft,
            y: element.clientTop,
            width: element.clientWidth,
            height: element.clientHeight,
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

      const browser = await this.newBrowser();
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
}

function isNumber(value): boolean {
  return typeof value === 'number' && isFinite(value);
}
