import { Browser, Page } from 'puppeteer';
import puppeteer from 'puppeteer-extra';
import { Injectable, Logger } from '@nestjs/common';

export interface PuppeteerResponse {
  screenshot?: String | Buffer;
  html?: String;
  isError: Boolean;
  errorMessage?: String;
}

async function grab(page: Page, optimize: boolean) {
  const html = await page.evaluate(() => {
    return document.documentElement.outerHTML;
  });

  if (optimize) {
    return { html, screenshot: null };
  }

  const screenshot = await page.screenshot({ encoding: 'base64', type: 'png' });

  return { html, screenshot };
}

// todo use blocklist to speed up https://github.com/jmdugan/blocklists/tree/master/corporations
@Injectable()
export class PuppeteerService {
  private readonly logger = new Logger(PuppeteerService.name);

  async launchBrowser(): Promise<Browser> {
    const debug = process.env.DEBUG === 'true';
    // see https://github.com/GoogleChrome/puppeteer/blob/master/docs/troubleshooting.md

    if (process.env.CHROME_HEADLESS_URL) {
      this.logger.log(`CHROME_HEADLESS_URL ${process.env.CHROME_HEADLESS_URL}`);
      return puppeteer.connect({
        browserURL: process.env.CHROME_HEADLESS_URL,
        defaultViewport: {
          width: 1024,
          height: 768,
        },
      });
    }
    return PuppeteerService.launchLocal(debug);
  }

  private static launchLocal(debug: boolean) {
    return puppeteer.launch({
      headless: !debug,
      defaultViewport: {
        width: 1024,
        height: 768
      },
      timeout: 10000,
      dumpio: debug,
      args: [
        '--disable-dev-shm-usage',
        // '--disable-background-networking',
        // Disable installation of default apps on first run
        '--disable-default-apps',
        // Disable all chrome extensions entirely
        '--disable-extensions',
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
        // Expose port 9222 for remote debugging
        //  '--remote-debugging-port=9222',
        // Disable fetching safebrowsing lists, likely redundant due to disable-background-networking
        '--safebrowsing-disable-auto-update'
      ]
    });
  }

  private async createPage(optimize: boolean = false): Promise<Page> {
    const browser = await this.launchBrowser();
    const page = await browser.newPage();
    await page.setCacheEnabled(false);
    await page.setBypassCSP(true);
    // await page.setExtraHTTPHeaders({
    //   'Accept-Language': 'en',
    // });
    // await page.setUserAgent(
    //   'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36',
    // );

    if (optimize) {
      await page.setRequestInterception(true);
      page.on('request', (req: any) => {
        if (
          req.resourceType() == 'stylesheet' ||
          req.resourceType() == 'font' ||
          req.resourceType() == 'image'
        ) {
          req.abort();
        } else {
          req.continue();
        }
      });
    }
    return page;
  }

  public async getMarkup(
    cid: string,
    url: string,
    beforeScript: string,
    optimize: boolean,
    timeoutMillis = 5000,
  ): Promise<PuppeteerResponse> {
    const page = await this.createPage(optimize);
    return this.runPage(cid, url, page, timeoutMillis, beforeScript, optimize);
  }

  async runPage(cid: string, url: string, page: Page, timeoutMillis: number, beforeScript: string, optimize: boolean) {
    this.logger.log(`[${cid}] goto ${url}`);

    function destroyPage() {
      // return page.browser().close().catch();
      return page.close()
    }

    // setTimeout(() => , 60000);
    await page.goto(url, {
      waitUntil: 'domcontentloaded',
      timeout: timeoutMillis
    });
    try {
      page.setDefaultTimeout(timeoutMillis);
      // await page.setCookie({
      //   name: 'CONSENT',
      //   value: 'YES+cb.20211102-08-p0.en+FX+706',
      // });
      await this.runBeforeScript(cid, page, beforeScript);
      await page.waitForTimeout(500);

      const { html, screenshot } = await grab(page, optimize);
      await destroyPage();

      return { screenshot, isError: false, html };
    } catch (e) {
      this.logger.log(`[${cid}] ${e.message}`);
      const { html, screenshot } = await grab(page, optimize);
      await destroyPage();

      return { errorMessage: e.message, screenshot, isError: true, html };
    }
  }

// http://localhost:3000/api/intern/prerender?url=https://derstandard.at

  private async runBeforeScript(cid: string, page: Page, beforeScript: string) {
    if (beforeScript) {
      this.logger.log(`[${cid}] running beforeScript`);
      const lines = beforeScript
        .split('\n')
        .map((lin) => lin.trim())
        .filter((line) => line.length > 0);
      await lines.reduce(
        (waitFor, line) =>
          waitFor.then(async () => await this.runLine(cid, page, line)),
        Promise.resolve(),
      );
    } else {
      this.logger.log(`[${cid}] no beforeScript provided`);
    }
  }

  private async runLine(cid: string, page: Page, line: string) {
    const parts = line.split(';').map((p) => p.trim());

    switch (parts[0]) {
      case 'clickXPath':
        this.logger.log(`[${cid}] clickXPath '${parts[1]}'`);
        const clickable = await page.waitForXPath(parts[1]);
        await clickable.evaluate((b) => (b as any).click());
        break;
      case 'select':
        this.logger.log(`[${cid}] select '${parts[1]}' '${parts[2]}'`);
        await page.select(parts[1], parts[2]);
        break;
      case 'waitForXPath':
        this.logger.log(`[${cid}] waitForXPath '${parts[1]}'`);
        await page.waitForXPath(parts[1]);
        break;
      default:
        throw new Error(`Cannot parse line '${line}'`);
    }
  }
}
