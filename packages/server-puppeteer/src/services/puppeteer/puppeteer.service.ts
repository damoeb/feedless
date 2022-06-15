import { Browser, Page } from 'puppeteer';
import puppeteer from 'puppeteer-extra';
import { Injectable, Logger } from '@nestjs/common';
import { PuppeteerJob } from './puppeteer.controller';

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
  private readonly isDebug: boolean;
  private readonly queue: {
    job: PuppeteerJob;
    resolve: (response: PuppeteerResponse) => void;
    reject: (reason: string) => void;
  }[] = [];
  private readonly maxWorkers = 5;
  private currentActiveWorkers = 0;

  constructor() {
    this.isDebug = process.env.DEBUG === 'true';
  }

  private static launchLocal(debug: boolean) {
    return puppeteer.launch({
      headless: !debug,
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
        '--safebrowsing-disable-auto-update',
      ],
    });
  }

  public async submit(job: PuppeteerJob): Promise<PuppeteerResponse> {
    return new Promise<PuppeteerResponse>((resolve, reject) => {
      this.queue.push({ job, resolve, reject });
      if (this.currentActiveWorkers < this.maxWorkers) {
        this.startWorker(this.currentActiveWorkers);
      }
    });
  }

  private async newBrowser(): Promise<Browser> {
    return PuppeteerService.launchLocal(this.isDebug);
  }

  private async request(
    { corrId, url, beforeScript, optimize, timeoutMillis }: PuppeteerJob,
    browser: Browser,
  ): Promise<PuppeteerResponse> {
    this.logger.log(`[${corrId}] goto ${url}`);

    const page = await browser.newPage();
    await page.setCacheEnabled(false);
    await page.setBypassCSP(true);

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

    await page.goto(url, {
      waitUntil: 'domcontentloaded',
      timeout: timeoutMillis,
    });
    try {
      if (beforeScript) {
        await page.evaluate(beforeScript);
      }

      const { html, screenshot } = await grab(page, optimize);
      return { screenshot, isError: false, html };
    } catch (e) {
      this.logger.log(`[${corrId}] ${e.message}`);
      const { html, screenshot } = await grab(page, optimize);
      return { errorMessage: e.message, screenshot, isError: true, html };
    }
  }

  // http://localhost:3000/api/intern/prerender?url=https://derstandard.at

  private async startWorker(workerId: number) {
    this.logger.log(`startWorker #${workerId}`);
    this.currentActiveWorkers++;
    while (this.queue.length > 0) {
      const { job, resolve, reject } = this.queue.shift();
      this.logger.log(
        `worker #${workerId} consumes [${job.corrId}] ${job.url}`,
      );

      const browser = await this.newBrowser();
      try {
        const forceTimeout = new Promise<PuppeteerResponse>((_, reject) =>
          setTimeout(
            () => reject('timeout exceeded'),
            job.timeoutMillis - 1000,
          ),
        );
        const response = await Promise.race([
          forceTimeout,
          await this.request(job, browser),
        ]);
        await browser.close();
        resolve(response);
      } catch (e) {
        await browser.close();
        reject(e.message);
      }
    }
    this.currentActiveWorkers--;
    this.logger.log(`endWorker #${workerId}`);
  }
}
