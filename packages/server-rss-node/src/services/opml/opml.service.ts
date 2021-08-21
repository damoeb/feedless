import { Injectable, Logger } from '@nestjs/common';
import { User } from '.prisma/client';
import { PrismaService } from '../../modules/prisma/prisma.service';
import { OpmlOutline, OpmlParser } from './opml-parser';
import { FeedService } from '../feed/feed.service';

interface BucketRef {
  title: string;
  filter_expression?: string;
  feeds: FeedRef[];
}
interface FeedRef {
  title: string;
  feed_url: string;
  tags?: string[];
  home_page_url: string;
  broken: boolean;
}

@Injectable()
export class OpmlService {
  private readonly logger = new Logger(OpmlService.name);

  constructor(
    private readonly prismaService: PrismaService,
    private readonly feedService: FeedService,
  ) {}

  async createBucketsFromOpml(opmlBase64: string, user: User) {
    console.log('create buckets');
    const document = new OpmlParser().parseOpml(opmlBase64);

    const buckets = await this.createBuckets(document.body);

    await buckets.reduce((waitFor, bucket) => {
      return waitFor.then(async () => {
        console.log('bucket', bucket.title, bucket.filter_expression);
        await this.prismaService.bucket.create({
          data: {
            title: bucket.title,
            filter_expression: bucket.filter_expression,
            owner: {
              connect: {
                id: user.id,
              },
            },
            stream: { create: {} },
            subscriptions: {
              create: bucket.feeds.map((feed) => {
                console.log(`feed ${feed.feed_url}`);
                return {
                  owner: {
                    connect: {
                      id: user.id,
                    },
                  },
                  title: feed.title,
                  feed: {
                    create: {
                      feed_url: feed.feed_url,
                      home_page_url: feed.home_page_url,
                      broken: feed.broken,
                      stream: {
                        create: {},
                      },
                    },
                  },
                };
              }),
            },
          },
        });
      });
    }, Promise.resolve());
  }

  private async getFeedRef(outline: OpmlOutline): Promise<FeedRef> {
    if (outline.xmlUrl) {
      return this.completeFromXmlUrl(outline);
    } else if (outline.htmlUrl) {
      return this.completeFromHtmlUrl(outline);
    } else if (outline.query) {
      return this.completeWithQuery(outline);
    } else {
      throw new Error('Outline does not point to any url/query');
    }
  }

  private async completeFromXmlUrl(outline: OpmlOutline): Promise<FeedRef> {
    if (!outline.xmlUrl) {
      throw new Error('xmlUrl is undefined');
    }
    const feed = await this.feedService.getFeedForUrl(outline.xmlUrl);
    return {
      title: outline.title || feed.title,
      feed_url: outline.xmlUrl,
      home_page_url: feed.home_page_url,
      broken: false,
    };
  }

  private async completeWithQuery(outline: OpmlOutline): Promise<FeedRef> {
    if (!outline.title) {
      throw new Error('outline.title is undefined');
    }

    return {
      title: outline.title,
      feed_url: `http://localhost:8080/api/feeds/query?q=${outline.query}`,
      home_page_url: '',
      broken: false,
    };
  }

  private async completeFromHtmlUrl(
    outline: OpmlOutline,
  ): Promise<FeedRef | null> {
    const htmlUrl = outline.htmlUrl;
    if (!htmlUrl) {
      throw new Error('htmlUrl is not set');
    }

    const url = new URL(htmlUrl);
    if (
      url.hostname === 'www.youtube.com' &&
      url.pathname.indexOf('channel/') > -1
    ) {
      // https://www.youtube.com/channel/UCc8_dv1ysWAo4LzyJH3Xuww
      const channel = url.pathname.split('/')[2];
      // https://www.youtube.com/feeds/videos.xml?channel_id=UCBR8-60-B28hp2BmDPdntcQ
      const feedUrl = `https://www.youtube.com/feeds/videos.xml?channel_id=${channel}`;
      console.log(`-> ${feedUrl}`);
      return {
        title: outline.title || `yt@${channel}`,
        feed_url: feedUrl,
        home_page_url: htmlUrl,
        tags: ['CONTENT:video'],
        broken: false,
      };
    }

    const feeds = (
      await this.feedService.discoverFeedsByUrl(htmlUrl)
    ).nativeFeeds.filter((feed) => feed);
    if (feeds.length === 0) {
      console.warn(`-> broken, cause no feeds detected`);
      return {
        title: outline.title || htmlUrl,
        feed_url: htmlUrl,
        home_page_url: htmlUrl,
        broken: true,
      };
    }
    const discoveredUrl = feeds[0];

    if (feeds.length > 1) {
      console.log(
        `-> ${discoveredUrl.feed_url} [${feeds
          .slice(1, feeds.length)
          .map((feed) => feed.feed_url)}]`,
      );
    } else {
      console.log(`-> ${discoveredUrl.feed_url}`);
    }
    const feed = feeds[0];
    return {
      title: outline.title || feed.title,
      feed_url: feed.feed_url,
      home_page_url: htmlUrl,
      broken: false,
    };
  }

  private createBuckets(outlines: OpmlOutline[]): Promise<BucketRef[]> {
    return outlines.reduce(async (waitForBuckets, outline) => {
      return waitForBuckets.then(async (buckets) => {
        const bucket: BucketRef = {
          title: outline.title,
          filter_expression: outline.filter,
          feeds: await (outline.outlines || []).reduce(
            (waitForFeeds, otherOutline) => {
              return waitForFeeds.then(async (feeds) => {
                const feed = await this.getFeedRef(otherOutline).catch(
                  console.error,
                );
                if (feed) {
                  feeds.push(feed);
                }
                return feeds;
              });
            },
            Promise.resolve([] as FeedRef[]),
          ),
        };
        buckets.push(bucket);
        return buckets;
      });
    }, Promise.resolve([] as BucketRef[]));
  }
}
