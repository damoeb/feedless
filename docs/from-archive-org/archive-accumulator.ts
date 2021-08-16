import {filter, flatten, head, size, tail, times, uniq, uniqBy} from 'lodash';
import * as request from "request";
import {Request, Response} from "request";
import * as FeedParser from "feedparser";
import {Item} from "feedparser";
import {Readable} from "stream";
import {JSDOM} from 'jsdom';
import Readability = require("mozilla-readability");


// todo use https://www.npmjs.com/package/dompurify
export interface Harvest {
  harvestLink: string
  data: string
}

export interface Job {
  url: string
  onSuccess: (body: any) => void
  onError: (error: string, httpResponse?: Response, body?: any) => void
}

class ArchiveAccumulator {
  constructor(private url: string) {
    this.harvest();
  }

  public async accumulate(): Promise<any> {
    const years = await this.downloadYears(this.url)
      .then(data => data.years)
      .then(years => {
        // todo filter
        return years;
      })
      .catch(body => {
        console.log(body);
        process.exit(1);
      });

    const nestedUrlsPromise = Object.keys(years).map(year => {
      return this.downloadHarvestsForYear(this.url, parseInt(year)).then(response =>
        Promise.all(response.items.map((item: any) => this.getFeedUrl(item[0], parseInt(year), this.url).catch(() => undefined))));
    });

    return Promise.all(nestedUrlsPromise).then(nestedUrls => uniq(flatten(nestedUrls)).filter((url: string) => size(url) > 0))
      .then(async (feedUrls: string[]) => {
        const nestedItems: Item[][] = await Promise.all(feedUrls.map(feedUrl => {
          return this.fetch(feedUrl)
            .then(feedContent => this.processFeed(feedContent))
            .catch(() => []);
        }));

        this.log(nestedItems.length, 'feed groups');
        const articles = uniqBy(flatten(nestedItems), 'link');
        this.log(articles.length, 'feed items');

        this.log('Resolving readability');

        return Promise.all(articles.map(article => {
          return this.firstHarvest(article.link).then(harvest => {
            const articleWithReadability: any = article;
            articleWithReadability.url = harvest.harvestLink;
            articleWithReadability.readability = this.getReadability(harvest.data);
            console.log(`${this.url} ${article.link} ${JSON.stringify(articleWithReadability)}`);
            return article;
          }).catch(error => {
            console.warn('Unable to get readability', error);
            console.log(`${this.url} ${article.link} ${JSON.stringify(article)}`)
          });
        }))
      }).catch(err => console.error('Aborted', err));

  }

  private firstHarvest(siteUrl: string): Promise<Harvest> {
    return this.downloadYears(this.url).then(harvests => {
      const firstTs = harvests.first_ts;
      const directLink = `https://web.archive.org/web/${firstTs}if_/${siteUrl}`;
      return this.fetch(directLink).then(data => {
        return {harvestLink: directLink, data}
      });
    })
  }


  // https://web.archive.org/__wb/sparkline?url=https%3A%2F%2Fwww.artofmanliness.com%2Fcategory%2Fpodcast%2Ffeed%2F&collection=web&output=json
  private downloadYears(feedUrl: string): Promise<any> {
    const url = `https://web.archive.org/__wb/sparkline?url=${encodeURIComponent(feedUrl)}&collection=web&output=json`;
    return this.fetch(url).then(body => {
      try {
        return JSON.parse(body)
      } catch (e) {
        return Promise.reject(body);
      }
    });
  }

  private getFeedUrl(partialTimestamp: number, year: number, url: string): Promise<string> {
    // https://web.archive.org/web/2017412033455/https://www.artofmanliness.com/category/podcast/feed/
    // https://web.archive.org/web/20171116151425if_/http://www.artofmanliness.com:80/category/podcast/feed/
    return this.fetch(`https://web.archive.org/web/${year}${partialTimestamp}/${url}`).then(body => {
      const needle = '<iframe id="playback" src="';
      if (!body) {
        return Promise.reject();
      }
      const fromIndex = body.indexOf(needle);
      const toIndex = body.indexOf('"', fromIndex + needle.length)
      const url = body.substring(fromIndex + needle.length, toIndex);
      if (!this.validURL(url)) {
        // console.error('cannot find feedUrl in body', body);
        // process.exit(1);
        this.log(`cannot find feedUrl, found ${url}`);
        return Promise.reject();
      }
      return url;
    });
  }

  private validURL(str: string): boolean {
    return str.startsWith('http://') || str.startsWith('https://');
  }

// https://web.archive.org/__wb/calendarcaptures/2?url=https%3A%2F%2Fwww.artofmanliness.com%2Fcategory%2Fpodcast%2Ffeed%2F&date=2014
  private downloadHarvestsForYear(feedUrl: string, year: number): Promise<any> {

    // https://web.archive.org/__wb/calendarcaptures/2?url=https%3A%2F%2Fwww.artofmanliness.com%2Fcategory%2Fpodcast%2Ffeed%2F&date=2012
    const url = `https://web.archive.org/__wb/calendarcaptures/2?url=${encodeURIComponent(feedUrl)}&date=${year}`;
    return this.fetch(url).then(body => JSON.parse(body));
  }

  private jobs: Job[] = [];
  private busy: boolean[];

  private harvest(threads = 4): void {
    this.busy = times(threads).map(() => false);
    this.busy.forEach((_, index) => {
      setInterval(() => {
        if (!this.busy[index] && this.jobs.length > 0) {
          try {
            this.busy[index] = true;
            let req: Request;
            const masterJob = head(this.jobs);
            this.jobs = tail(this.jobs);
            // this.log(`${this.jobs.length} jobs waiting`);

            const slaveJobs = filter(this.jobs, {url: masterJob.url})
            this.log(`Fetching ${masterJob.url}`);

            const currentJobs: Job[] = [masterJob, ...slaveJobs];

            const timeoutId = setTimeout(() => {
              this.log('Aborting request due to timeout');
              if (req) {
                this.log('Aborting...');
                req.abort();
                currentJobs.forEach(currentJob => {
                  currentJob.onError('aborted');
                })

                this.busy[index] = false;
              }
            }, 10000);

            const now = new Date().getTime();
            // todo migrate to bent, since request is deprecated
            req = request(masterJob.url, (error, httpResponse: Response, body) => {
              clearTimeout(timeoutId);
              if (error) {
                this.log(`Error fetching ${masterJob.url}`, error);
                console.error('With error', error);
                currentJobs.forEach(currentJob => {
                  currentJob.onError(error, httpResponse, body);
                });
              } else {
                this.log(`OK [${new Date().getTime() - now} msec, ${this.jobs.length} jobs]`);
                currentJobs.forEach(currentJob => {
                  currentJob.onSuccess(body);
                });
              }
              this.busy[index] = false;
            });

          } catch (err) {
            this.busy[index] = false;
          }
        }

      }, Math.floor(Math.random() * 500) + 1);
    });
  }

  private fetch(url: string): Promise<any> {
    return new Promise<any>((resolve, reject) => {
      const job = {
        url,
        onSuccess: resolve,
        onError: reject
      }
      this.jobs.push(job);
    });
  }

  private processFeed(xmlBody: string): Promise<Item[]> {
    return new Promise<Item[]>((resolve, reject) => {
      try {
        const feedParser = new FeedParser({normalize: true, resume_saxerror: true});
        // const readable = Readable.from(xmlBody);
        const readable = new Readable();
        readable.push(xmlBody);
        readable.push(null)

        readable.pipe(feedParser);
        const posts: Item[] = [];

        feedParser.on('error', (err: Error) => {
          reject(`${err.message}`)
        });
        feedParser.on('end', () => {
          resolve(posts);
        });
        feedParser.on('readable', () => {
          let post;
          while (post = feedParser.read()) {
            posts.push(post);
          }
        });
      } catch (e) {
        console.error('Cannot parse feed', e);
        reject();
      }
    })
  }

  private getReadability(staticHtml: string): Readability.ParseResult {
    const dom = new JSDOM(staticHtml);
    const readability = new Readability(dom.window.document);
    return readability.parse();
  }

  private log(...args: any[]) {
    console.error.apply(this, args)
  }
}


function parseParams(): Promise<string[]> {
  return new Promise<string[]>((resolve, reject) => {
    if (process.argv.length === 3) {
      resolve([process.argv[2]]);
    } else {
      // process.stdin.setRawMode(true);
      process.stdin.on('readable', () => {
        const urls = String(process.stdin.read());
        if (urls !== null) {
          resolve(urls.split('\n'));
        }
      });

      process.stdin.on('error', () => {
        reject();
      });
    }
  });

}

parseParams()
  .then(urls => {
    urls.reduce((waitFor, url) => {
      return waitFor.then(() => {
        const accumulator = new ArchiveAccumulator(url);
        console.error(`Starting harvesting ${url}`);
        return accumulator.accumulate().then(() => new Promise(resolve => {
          console.error(`Finished harvesting ${url}`);
          setTimeout(resolve, 5000);
        }));
      });
    }, Promise.resolve());
  })
  .catch(() => {
    console.log('Missing argument [urls]')
    process.exit(1);
  });

