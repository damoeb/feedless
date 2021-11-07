import { Injectable, Logger } from '@nestjs/common';
import fetch, { Response } from 'node-fetch';
import { uniqBy } from 'lodash';
import { ArticleRef, Feed, Subscription } from '@generated/type-graphql/models';
import dayjs from 'dayjs';
import { PrismaService } from '../../modules/prisma/prisma.service';
import {
  DiscoveredFeeds,
  NativeFeedRef,
} from '../../modules/typegraphql/feeds';
import { RssProxyService } from '../rss-proxy/rss-proxy.service';
import { CustomFeedResolverService } from '../custom-feed-resolver/custom-feed-resolver.service';

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
  name: string;
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
    private readonly rssProxyService: RssProxyService,
    private readonly customFeedResolver: CustomFeedResolverService,
  ) {}

  async discoverFeedsByUrl(
    urlParam: string,
    skipRssProxy = true,
    email?: string,
  ): Promise<DiscoveredFeeds> {
    try {
      const url = this.fixUrl(urlParam);
      this.logger.log(`Discovering feeds in ${url}`);
      const encodedUrl = encodeURIComponent(url);
      const response: Response = await fetch(
        `http://localhost:8080/api/feeds/discover?url=${encodedUrl}`,
        { timeout: 5000 },
      );

      if (response.status === 200) {
        const { feeds, body } = (await response.json()) as any;
        const nativeFeeds = feeds.map(
          (feedRef) => new NativeFeedRef(feedRef.url, feedRef.title, url),
        );

        let customFeeds: NativeFeedRef[] = [];
        if (email && this.customFeedResolver && body) {
          try {
            customFeeds = await this.customFeedResolver.applyCustomResolvers(
              email,
              url,
              body,
            );
          } catch (e) {
            // ignore
          }
        }

        const allNativeFeeds = uniqBy(
          [...nativeFeeds, ...customFeeds],
          'feed_url',
        );

        if (!skipRssProxy && body) {
          return {
            nativeFeeds: allNativeFeeds,
            generatedFeeds: await this.rssProxyService
              .parseFeeds(url, body)
              .catch(() => null),
          };
        }

        return {
          nativeFeeds: allNativeFeeds,
        };
      } else {
        throw new Error(`Invalid http-status ${response.status}`);
      }
    } catch (e) {
      console.error(`Failed discoverFeedsByUrl ${urlParam}`, e);
    }
    return new DiscoveredFeeds();
  }

  async getFeedForUrl(url: string): Promise<Feed> {
    if (url.indexOf('/api/rss-proxy') === -1) {
      return this.getNativeFeedForUrl(url);
    } else {
      return this.getGeneratedProxyFeedForUrl(url);
    }
  }
  async getNativeFeedForUrl(url: string): Promise<Feed> {
    const rawFeed: RawFeed = (await fetch(
      `http://localhost:8080/api/feeds/parse?url=${encodeURIComponent(url)}`,
    ).then((res) => res.json())) as any;
    return {
      id: rawFeed.id,
      title: rawFeed.name,
      description: rawFeed.description,
      feed_url: rawFeed.feed_url,
      is_private: false,
      expired: false,
      status: 'ok',
      createdAt: new Date(),
      ownerId: 'system',
      broken: false,
      inactive: false,
      home_page_url: rawFeed.home_page_url,
      stream: {
        id: '',
        articleRefs: (rawFeed.items || []).map((entry) =>
          FeedService.toArticle(entry),
        ),
      },
      streamId: '',
    };
  }
  async getGeneratedProxyFeedForUrl(url: string): Promise<Feed> {
    const rawFeed: RawFeed = (await fetch(
      url.replace('/api/rss-proxy', '/api/rss-proxy/json'),
    ).then((res) => res.json())) as any;
    return {
      id: rawFeed.id,
      title: rawFeed.name,
      description: rawFeed.description,
      feed_url: rawFeed.feed_url,
      is_private: false,
      expired: false,
      status: 'ok',
      ownerId: 'system',
      createdAt: new Date(),
      broken: false,
      inactive: false,
      home_page_url: rawFeed.home_page_url,
      stream: {
        id: '',
        articleRefs: (rawFeed.items || []).map((entry) =>
          FeedService.toArticle(entry),
        ),
      },
      streamId: '',
    };
  }

  private static toArticle(entry: RawEntry): ArticleRef {
    return {
      id: '',
      ownerId: 'system',
      createdAt: new Date(),
      date_released: new Date(),
      favored: false,
      has_seen: false,
      articleId: entry.id,
      article: {
        id: entry.id,
        url: entry.url,
        title: entry.title,
        score: 0,
        released: true,
        lastScoredAt: new Date(),
        applyPostProcessors: false,
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
        const rawFeed = await this.getFeedForUrl(feedUrl);
        existingFeed = await this.prisma.feed.create({
          data: {
            feed_url: feedUrl,
            title: rawFeed.title,
            description: rawFeed.description,
            home_page_url: rawFeed.home_page_url || feedUrl,
            expired: rawFeed.expired,
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
}
