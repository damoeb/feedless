import { Injectable, Logger } from '@nestjs/common';
import { Cluster } from 'puppeteer-cluster';
import { PuppeteerResponse, PuppeteerService } from './puppeteer.service';

// todo use blocklist to speed up https://github.com/jmdugan/blocklists/tree/master/corporations
@Injectable()
export class PuppeteerClusterService {
  private readonly logger = new Logger(PuppeteerClusterService.name);
  private cluster: Cluster<any, any>;

  constructor(private readonly puppeteer: PuppeteerService) {
    this.init();
  }

  // https://stackoverflow.com/questions/59672126/is-puppeteer-cluster-stealthy-enough-to-pass-bot-tests
  // https://github.com/thomasdondorf/puppeteer-cluster/
  private async init() {
    this.cluster = await Cluster.launch({
      concurrency: Cluster.CONCURRENCY_CONTEXT,
      maxConcurrency: 2,
      monitor: false, //true,
      sameDomainDelay: 1000,
      retryDelay: 3000,
      workerCreationDelay: 3000,
      puppeteerOptions: {
        args: [
          // "--proxy-server=pro.proxy.net:2222",
          '--incognito',
          '--disable-gpu',
          '--disable-dev-shm-usage',
          '--disable-setuid-sandbox',
          '--no-first-run',
          '--no-sandbox',
          '--no-zygote',
        ],
        headless: false,
      },
    });
  }

  public async getMarkup(
    cid: string,
    url: string,
    beforeScript: string,
    optimize: boolean,
    timeoutMillis = 5000,
  ): Promise<PuppeteerResponse> {
    return new Promise((resolve) => {
      const data = {
        cid,
        url,
        beforeScript,
        optimize,
        timeoutMillis,
      };
      this.cluster.execute(data, async ({ page, data }) => {
        resolve(
          this.puppeteer.runPage(
            data.cid,
            data.url,
            page,
            data.timeoutMillis,
            data.beforeScript,
            data.optimize,
          ),
        );
      });
    });
  }
}
