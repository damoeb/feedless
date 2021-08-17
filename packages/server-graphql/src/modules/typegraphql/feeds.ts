import {
  Arg,
  Ctx,
  Field,
  Mutation,
  ObjectType,
  Query,
  Resolver,
} from 'type-graphql';
import { FeedService } from '../../services/feed/feed.service';
import { Article, Feed, Subscription } from '@generated/type-graphql/models';
import { ProxyFeeds } from '../../services/rss-proxy/rss-proxy.service';
import { Logger } from '@nestjs/common';

@ObjectType()
export class NativeFeedRef {
  constructor(feedUrl: string, title: string, homepageUrl: string) {
    this.home_page_url = homepageUrl;
    this.feed_url = feedUrl;
    this.title = title;
  }
  @Field()
  feed_url: string;
  @Field()
  home_page_url: string;
  @Field()
  title: string;
  @Field({ nullable: true })
  description?: string;
}

@ObjectType()
export class DiscoveredFeeds {
  constructor(nativeFeeds: NativeFeedRef[] = []) {
    this.nativeFeeds = nativeFeeds;
  }

  @Field(() => [NativeFeedRef], { nullable: true })
  nativeFeeds: NativeFeedRef[];
  @Field({ nullable: true })
  generatedFeeds: ProxyFeeds;
}

@Resolver()
export class Feeds {
  private readonly logger = new Logger(Feeds.name);

  @Query(() => DiscoveredFeeds)
  async discoverFeedsByUrl(
    @Ctx() context: any,
    @Arg('url', () => String) url: string,
  ): Promise<DiscoveredFeeds> {
    this.logger.log(`disoverFeeds for ${url}`);
    const feedService: FeedService = context.feedService;
    return feedService.discoverFeedsByUrl(url, false);
  }

  @Query(() => [Article])
  async articlesForFeedUrl(
    @Ctx() context: any,
    @Arg('feedUrl', () => String) feedUrl: string,
  ): Promise<Article[]> {
    this.logger.log(`articles for feed ${feedUrl}`);
    const feedService: FeedService = context.feedService;
    const feed = await feedService.getFeedForUrl(feedUrl);
    return feed.stream.articleRefs.map((ref) => ref.article);
  }

  @Query(() => Feed)
  async metadataForNativeFeedByUrl(
    @Ctx() context: any,
    @Arg('feedUrl', () => String) feedUrl: string,
  ): Promise<Feed> {
    this.logger.log(`articles for feed ${feedUrl}`);
    const feedService: FeedService = context.feedService;
    return feedService.getFeedForUrl(feedUrl);
  }

  @Mutation(() => Subscription)
  async subscribeToFeed(
    @Ctx() context: any,
    @Arg('feedUrl', () => String) feedUrl: string,
    @Arg('bucketId', () => String) bucketId: string,
    @Arg('email', () => String) email: string,
  ): Promise<Subscription> {
    const feedService: FeedService = context.feedService;
    return feedService.subscribeToFeed(feedUrl, bucketId, email);
  }
}
