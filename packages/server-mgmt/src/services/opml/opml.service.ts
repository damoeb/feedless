import { Injectable, Logger } from '@nestjs/common';
import { User } from '.prisma/client';
import { firstValueFrom } from 'rxjs';
import { PrismaService } from '../../modules/prisma/prisma.service';
import { OpmlOutline, OpmlParser } from './opml-parser';
import { FeedService } from '../feed/feed.service';
import { newCorrId } from '../../libs/corrId';

interface BucketRef {
  title: string;
  filter_expression?: string;
  post_processors?: string[];
  feeds: FeedRef[];
}
export interface FeedRef {
  title: string;
  feed_url: string;
  tags?: string[];
  home_page_url: string;
  is_private: boolean;
  owner: string;
  filter?: string;
  broken: boolean;
  retention_size?: number;
  harvest_with_prerender?: boolean;
  harvest_site?: boolean;
  allowHarvestFailure?: boolean;
}

const PUBLIC = 'system';

@Injectable()
export class OpmlService {
  private readonly logger = new Logger(OpmlService.name);

  constructor(
    private readonly prisma: PrismaService,
    private readonly feedService: FeedService,
  ) {}

  async createBucketsFromOpml(opmlBase64: string, user: User) {
    console.log('create buckets');
    const document = new OpmlParser().parseOpml(opmlBase64);

    console.log(`owner ${document.head.ownerEmail}`);
    const owner = await this.prisma.user.findUnique({
      where: {
        email: document.head.ownerEmail,
      },
    });

    const buckets = await this.createBuckets(document.body, owner);

    await buckets.reduce((waitFor, bucket) => {
      return waitFor.then(async () => {
        console.log('bucket', bucket.title, bucket.filter_expression);
        await this.prisma.bucket.create({
          data: {
            title: bucket.title,
            // filter: fbucket.filter_expression,
            owner: {
              connect: {
                id: user.id,
              },
            },
            postProcessors: {
              create: (bucket.post_processors || []).map((pp) => ({
                type: pp.toUpperCase(),
              })),
            },
            stream: { create: {} },
            subscriptions: {
              create: bucket.feeds.map((feed) => {
                console.log(
                  `feed ${feed.feed_url} @ ${feed.owner} ` +
                    (feed.is_private ? '(private)' : ''),
                );
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
                      domain: new URL(feed.home_page_url).host,
                      broken: feed.broken,
                      filter: feed.filter,
                      is_private: feed.is_private,
                      // ownerId: feed.broken ? 'system' : 'system',
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

  private async getFeedRefs(
    outline: OpmlOutline,
    owner: string,
  ): Promise<FeedRef[]> {
    if (outline.xmlUrl) {
      return [await this.completeFromXmlUrl(outline)];
    } else if (outline.htmlUrl) {
      return [await this.completeFromHtmlUrl(outline, owner)];
    } else if (outline.query) {
      return [await this.completeWithQuery(outline, owner)];
    } else if (outline.outlines) {
      // nested node
      return outline.outlines.reduce((waitFor, nestedOutline) => {
        return waitFor.then(async (acc) => {
          try {
            const feedRefs = await this.getFeedRefs(nestedOutline, owner);
            acc.push(...feedRefs);
          } catch (e) {
            console.error(e);
          }
          return acc;
        });
      }, Promise.resolve([]));
    } else {
      throw new Error('Outline does not point to any url/query');
    }
  }

  private async completeFromXmlUrl(outline: OpmlOutline): Promise<FeedRef> {
    if (!outline.xmlUrl) {
      throw new Error('xmlUrl is undefined');
    }
    const feed = await firstValueFrom(
      this.feedService.getFeedForUrl(outline.xmlUrl),
    );
    return {
      title: outline.title || feed.title,
      feed_url: outline.xmlUrl,
      home_page_url: feed.home_page_url,
      filter: feed.filter,
      broken: false,
      owner: PUBLIC,
      is_private: false,
    };
  }

  private async completeWithQuery(
    outline: OpmlOutline,
    owner: string,
  ): Promise<FeedRef> {
    const title = outline.title || `Search '${outline.query}'`;

    const feedUrl = `http://localhost:8080/api/feeds/query?q=${encodeURIComponent(
      outline.query,
    )}`;
    return {
      title,
      feed_url: feedUrl,
      home_page_url: feedUrl,
      owner,
      broken: false,
      is_private: false,
    };
  }

  private async completeFromHtmlUrl(
    outline: OpmlOutline,
    owner: string,
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
        owner: PUBLIC,
        is_private: false,
      };
    }

    const feeds = (
      await this.feedService
        .discoverFeedsByUrl(newCorrId(), htmlUrl)
        .catch(() => ({ nativeFeeds: [] }))
    ).nativeFeeds.filter((feed) => feed);
    if (feeds.length === 0) {
      console.warn(`-> broken, cause no feeds detected`);
      return {
        title: outline.title || htmlUrl,
        feed_url: htmlUrl,
        home_page_url: htmlUrl,
        broken: true,
        owner,
        is_private: true,
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
      owner: PUBLIC,
      is_private: false,
    };
  }

  private createBuckets(
    outlines: OpmlOutline[],
    owner: User,
  ): Promise<BucketRef[]> {
    return outlines.reduce(async (waitForBuckets, outline) => {
      return waitForBuckets.then(async (buckets) => {
        const bucket: BucketRef = {
          title: outline.title,
          filter_expression: outline.filter,
          post_processors: outline.pp,
          feeds: await (outline.outlines || []).reduce(
            (waitForFeeds, otherOutline) => {
              return waitForFeeds.then(async (feeds) => {
                const feedRefs = await this.getFeedRefs(
                  otherOutline,
                  owner.id,
                ).catch(console.error);
                if (feedRefs && feedRefs.length > 0) {
                  feeds.push(...feedRefs);
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
