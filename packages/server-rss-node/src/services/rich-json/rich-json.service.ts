import { Injectable, Logger } from '@nestjs/common';
import { User } from '@prisma/client';
import { PrismaService } from '../../modules/prisma/prisma.service';
import { FeedService } from '../feed/feed.service';
import { newCorrId } from '../../libs/corrId';
import { FeedRef } from '../opml/opml.service';

export interface RootJson {
  buckets: BucketJson[];
}
export interface SubscriptionJson {
  title?: string;
  tags?: string[];
  htmlUrl?: string;
  xmlUrl?: string;
  query?: string;
}
export interface PipelineOperationJson {
  map: string;
  value: string;
}
export interface TriggerJson {
  expression: string;
  on: 'change' | 'scheduled';
}
export interface ExporterSegmentJson {
  sortField: string;
  sortDirection: string;
  size: number;
  digest: boolean;
}
export interface ExporterTargetJson {
  context: string;
  type: string;
}
export interface ExporterJson {
  trigger: TriggerJson;
  segment: ExporterSegmentJson;
  targets: ExporterTargetJson[];
}
export interface BucketJson {
  title: string;
  visibility: string;
  subscriptions: SubscriptionJson[];
  pipeline: PipelineOperationJson[];
  exporters: ExporterJson[];
}

@Injectable()
export class RichJsonService {
  private readonly logger = new Logger(RichJsonService.name);

  constructor(
    private readonly prisma: PrismaService,
    private readonly feedService: FeedService,
  ) {}

  async createBucketsFromRichJson(richJson: RootJson, user: User) {
    await richJson.buckets.reduce((waitFor, bucket) => {
      return waitFor.then(async () => {
        console.log('bucket', bucket.title);
        await this.prisma.bucket.create({
          data: {
            title: bucket.title,
            owner: {
              connect: {
                id: user.id,
              },
            },
            postProcessors: {
              create: (bucket.pipeline || []).map((pipelineOperation) => ({
                type: pipelineOperation.map.toUpperCase(),
                context: pipelineOperation.value,
              })),
            },
            stream: { create: {} },
            exporters: {
              create: (bucket.exporters || []).map((exporter) => {
                return {
                  segment: false,
                  // todo mag
                  // segment_digest: exporter.digest,
                  // segment_sort_field: '',
                  // segment_sort_asc: false,
                  // segment_size: exporter.segment,
                  targets: {
                    create: exporter.targets.map((target) => {
                      return {
                        type: target.type,
                        context: target.context,
                      };
                    }),
                  },
                };
              }),
            },
            subscriptions: {
              create: await Promise.all(
                bucket.subscriptions.map(async (subscription) => {
                  const feed = await this.getFeedRefs(subscription, user.id);

                  console.log(`feed ${feed.feed_url} @ ${feed.owner} `);
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
                        is_private: feed.is_private,
                        // ownerId: feed.broken ? 'system' : 'system',
                        stream: {
                          create: {},
                        },
                      },
                    },
                  };
                }),
              ),
            },
          },
        });
      });
    }, Promise.resolve());
  }

  private async getFeedRefs(
    subscription: SubscriptionJson,
    owner: string,
  ): Promise<FeedRef> {
    if (subscription.xmlUrl) {
      return this.completeFromXmlUrl(subscription);
    } else if (subscription.htmlUrl) {
      return this.completeFromHtmlUrl(subscription, owner);
    } else if (subscription.query) {
      return this.completeWithQuery(subscription, owner);
    } else {
      throw new Error('Outline does not point to any url/query');
    }
  }

  private async completeFromXmlUrl(
    subscription: SubscriptionJson,
  ): Promise<FeedRef> {
    if (!subscription.xmlUrl) {
      throw new Error('xmlUrl is undefined');
    }
    const feed = await this.feedService.getFeedForUrl(subscription.xmlUrl);
    return {
      title: subscription.title || feed.title,
      feed_url: subscription.xmlUrl,
      home_page_url: feed.home_page_url,
      broken: false,
      owner: 'system',
      is_private: false,
    };
  }

  private async completeWithQuery(
    subscription: SubscriptionJson,
    owner: string,
  ): Promise<FeedRef> {
    const title = subscription.title || `Search '${subscription.query}'`;

    const feedUrl = `http://localhost:8080/api/feeds/query?q=${encodeURIComponent(
      subscription.query,
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
    subscription: SubscriptionJson,
    owner: string,
  ): Promise<FeedRef | null> {
    const htmlUrl = subscription.htmlUrl;
    if (!htmlUrl) {
      throw new Error('htmlUrl is not set');
    }

    const feeds = (
      await this.feedService
        .discoverFeedsByUrl(newCorrId(), htmlUrl)
        .catch(() => ({ nativeFeeds: [] }))
    ).nativeFeeds.filter((feed) => feed);
    if (feeds.length === 0) {
      console.warn(`-> broken, cause no feeds detected`);
      return {
        title: subscription.title || htmlUrl,
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
      title: subscription.title || feed.title,
      feed_url: feed.feed_url,
      home_page_url: htmlUrl,
      broken: false,
      owner: 'system',
      is_private: false,
    };
  }
}
