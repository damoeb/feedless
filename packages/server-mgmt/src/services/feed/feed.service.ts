import { Injectable, Logger } from '@nestjs/common';
import { ArticleRef, Feed, Subscription } from '@generated/type-graphql/models';
import dayjs from 'dayjs';
import { PrismaService } from '../../modules/prisma/prisma.service';
import { DiscoveredFeeds, GenericFeedRule, NativeFeedRef } from '../../modules/typegraphql/feeds';
import { HttpService } from '@nestjs/axios';
import { firstValueFrom, map, Observable } from 'rxjs';

interface RawEntry {
  author: string;
  url: string;
  content_text: string;
  content_html?: string;
  id: string;
  tags?: string[];
  title: string;
  // enclosures: [];
  date_published: string; //'Jul 22, 2021, 4:55:00 PM';
}

interface RawFeed {
  id: string;
  title: string;
  expired?: boolean;
  description: string;
  author: string;
  date_published: string;
  home_page_url: string;
  feed_url: string;
  items: RawEntry[];
}

@Injectable()
export class FeedService {
  private readonly logger = new Logger(FeedService.name);

  constructor(
    private readonly prisma: PrismaService,
    private httpService: HttpService,
  ) {}

  async discoverFeedsByUrl(
    corrId: string,
    urlParam: string,
    prerender = false,
    email?: string,
  ): Promise<DiscoveredFeeds> {
    try {
      const homepageUrl = this.fixUrl(urlParam);
      const encodedUrl = encodeURIComponent(homepageUrl);
      const url = `http://localhost:8080/api/feeds/discover?correlationId=${corrId}&homepageUrl=${encodedUrl}&prerender=${prerender}`;
      this.logger.log(`[${corrId}] GET ${url}`);
      return firstValueFrom(this.httpService.get(url)).then((response) => {
        if (response.status === 200) {
          this.logger.log(`[${corrId}]  -> ${response.status}`);
          const { results } = response.data as any;
          const nativeFeeds = results?.nativeFeeds.map(
            (feedRef) =>
              new NativeFeedRef(
                feedRef.url,
                feedRef.type,
                feedRef.title,
                homepageUrl,
              ),
          );
          const genericFeedRules = results?.genericFeedRules.map(
            (rule) =>
              new GenericFeedRule(
                rule.feedUrl,
                rule.linkXPath,
                rule.extendContext,
                rule.contextXPath,
                rule.count,
                rule.score,
                rule.samples,
              ),
          );

          return {
            nativeFeeds,
            genericFeedRules,
          };
        } else {
          this.logger.error(`[${corrId}] -> ${response.status}`);
          throw new Error(`Invalid http-status ${response.status}`);
        }
      });
    } catch (e) {
      console.error(`Failed discoverFeedsByUrl ${urlParam}`, e);
    }
    return new DiscoveredFeeds();
  }

  getFeedForUrl(url: string): Observable<Partial<Feed>> {
    // if (url.indexOf('/api/web-to-feed') === -1) {
      return this.getNativeFeedForUrl(url);
    // } else {
    //   return this.getGeneratedProxyFeedForUrl(url);
    // }
  }
  getNativeFeedForUrl(url: string): Observable<Partial<Feed>> {
    return this.httpService
      .get<RawFeed>(
        `http://localhost:8080/api/feeds/transform?feedUrl=${encodeURIComponent(
          url,
        )}&format=json`,
      )
      .pipe(
        map((response) => {
          const rawFeed = response.data;
          const homepageUrl = this.getHomepageUrl(url, rawFeed);

          return {
            id: rawFeed.id,
            title: rawFeed.title,
            description: rawFeed.description,
            feed_url: rawFeed.feed_url,
            status: 'ok',
            createdAt: new Date(),
            home_page_url: homepageUrl,
            domain: new URL(homepageUrl).host,
            stream: {
              id: '',
              articleRefs: (rawFeed.items || []).map((entry) =>
                FeedService.toArticle(entry),
              ),
            },
          };
        }),
      );
  }
  // getGeneratedProxyFeedForUrl(url: string): Observable<Partial<Feed>> {
  //   return this.httpService
  //     .get<RawFeed>(url.replace('/api/web-to-feed', '/api/web-to-feed/json'))
  //     .pipe(
  //       map((response) => {
  //         const rawFeed = response.data;
  //         const feed: Partial<Feed> = {
  //           id: rawFeed.id,
  //           title: rawFeed.name,
  //           description: rawFeed.description,
  //           feed_url: rawFeed.feed_url,
  //           createdAt: new Date(),
  //           home_page_url: rawFeed.home_page_url,
  //           stream: {
  //             id: '',
  //             articleRefs: (rawFeed.items || []).map((entry) =>
  //               FeedService.toArticle(entry),
  //             ),
  //           },
  //           streamId: '',
  //           harvest_site: false,
  //           harvest_prerender: false,
  //           allowHarvestFailure: false,
  //         };
  //         return feed;
  //       }),
  //     );
  // }

  private static toArticle(entry: RawEntry): ArticleRef {
    return {
      id: '',
      ownerId: 'system',
      createdAt: new Date(),
      date_released: new Date(),
      favored: false,
      has_seen: false,
      articleId: entry.id,
      streamId: null,
      type: 'feed',
      article: {
        id: entry.id,
        url: entry.url,
        title: entry.title,
        score: 0,
        has_video: false,
        has_audio: false,
        length_video: 0,
        length_audio: 0,
        word_count_text: 0,
        released: true,
        lastScoredAt: new Date(),
        date_published: dayjs(entry.date_published).toDate(),
        updatedAt: dayjs(entry.date_published).toDate(),
        content_raw: entry.content_text,
        content_raw_mime: 'text/plain',
      },
    };
  }

  async subscribeToFeed(
    feedUrl: string,
    bucketId: string,
    email: string,
  ): Promise<Subscription> {
    try {
      this.logger.log(
        `subscribeToFeed feedUrl=${feedUrl} bucketId=${bucketId}`,
      );
      let existingFeed = await this.prisma.feed.findFirst({
        where: {
          feed_url: feedUrl,
          owner: {
            id: 'system',
          },
          is_private: false,
        },
      });
      if (!existingFeed) {
        const rawFeed = await firstValueFrom<Partial<Feed>>(this.getFeedForUrl(feedUrl));
        const homePageUrl = rawFeed.home_page_url || feedUrl;
        existingFeed = await this.prisma.feed.create({
          data: {
            feed_url: feedUrl,
            title: rawFeed.title,
            description: rawFeed.description,
            filter: null,
            home_page_url: homePageUrl,
            domain: new URL(homePageUrl).host,
            expired: rawFeed.expired,
            owner: {
              connect: {
                id: 'system'
              }
            },
            stream: {
              create: {},
            },
          },
        });
      }
      return await this.prisma.subscription.create({
        data: {
          title: existingFeed.title,
          bucket: {
            connect: {
              id: bucketId,
            },
          },
          feed: {
            connect: {
              id: existingFeed.id,
            },
          },
          owner: {
            connect: {
              email,
            },
          },
        },
      });
    } catch (e) {
      this.logger.error('Unable to subscribe', e);
      throw e;
    }
  }

  private fixUrl(urlParam: string): string {
    if (urlParam.startsWith('https://') || urlParam.startsWith('http://')) {
      return urlParam;
    } else {
      const fixedUrl = `https://${urlParam}`;
      new URL(fixedUrl);
      return fixedUrl;
    }
  }

  private getHomepageUrl(url: string, rawFeed: RawFeed) {
    return rawFeed.home_page_url || rawFeed.feed_url || url;
  }
}
