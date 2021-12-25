import { Arg, Ctx, Field, Mutation, ObjectType, Query, Resolver } from 'type-graphql';
import { FeedService } from '../../services/feed/feed.service';
import { Article, Feed, Subscription } from '@generated/type-graphql/models';
import { Logger } from '@nestjs/common';
import { newCorrId } from '../../libs/corrId';
import { firstValueFrom } from 'rxjs';

@ObjectType()
export class NativeFeedRef {
  constructor(
    feedUrl: string,
    feedType: string,
    title: string,
    homepageUrl: string,
  ) {
    this.home_page_url = homepageUrl;
    this.feed_url = feedUrl;
    this.feed_type = feedType;
    this.title = title;
  }
  @Field()
  feed_url: string;
  @Field()
  feed_type: string;
  @Field()
  home_page_url: string;
  @Field()
  title: string;
  @Field({ nullable: true })
  description?: string;
}

@ObjectType()
export class GenericFeedRule {
  constructor(
    feedUrl: string,
    linkXPath: string,
    extendContext: string,
    contextXPath: string,
    count: number,
    score: number,
    samples: Article[],
  ) {
    this.feed_url = feedUrl;
    this.linkXPath = linkXPath;
    this.extendContext = extendContext;
    this.contextXPath = contextXPath;
    this.count = count;
    this.score = score;
    this.samples = samples;
  }
  @Field()
  feed_url: string;
  @Field()
  linkXPath: string;
  @Field()
  extendContext: string;
  @Field()
  contextXPath: string;
  @Field()
  count: number;
  @Field()
  score: number;
  @Field(() => [Article])
  samples: Article[];
}

@ObjectType()
export class DiscoveredFeeds {
  constructor(nativeFeeds: NativeFeedRef[] = []) {
    this.nativeFeeds = nativeFeeds;
  }

  @Field(() => [NativeFeedRef], { nullable: true })
  nativeFeeds: NativeFeedRef[];

  @Field(() => [GenericFeedRule], { nullable: true })
  genericFeedRules: GenericFeedRule[];
}

@Resolver()
export class Feeds {
  private readonly logger = new Logger(Feeds.name);

  @Query(() => DiscoveredFeeds)
  async discoverFeedsByUrl(
    @Ctx() context: any,
    @Arg('url', () => String) url: string,
    @Arg('prerender', () => Boolean, { defaultValue: false })
    prerender: boolean,
  ): Promise<DiscoveredFeeds> {
    const corrId = newCorrId();
    this.logger.log(`[${corrId}] discoverFeeds for ${url}`);
    const feedService: FeedService = context.feedService;
    return feedService.discoverFeedsByUrl(
      corrId,
      url,
      prerender,
      'karl@may.ch',
    );
  }

  @Query(() => [Article])
  async articlesForFeedUrl(
    @Ctx() context: any,
    @Arg('feedUrl', () => String) feedUrl: string,
  ): Promise<Article[]> {
    this.logger.log(`articles for feed ${feedUrl}`);
    const feedService: FeedService = context.feedService;
    const feed = await firstValueFrom<Feed>(feedService.getFeedForUrl(feedUrl));
    return feed.stream.articleRefs.map((ref) => ref.article);
  }

  @Query(() => Feed)
  async metadataForNativeFeedByUrl(
    @Ctx() context: any,
    @Arg('feedUrl', () => String) feedUrl: string,
  ): Promise<Feed> {
    this.logger.log(`articles for feed ${feedUrl}`);
    const feedService: FeedService = context.feedService;
    return firstValueFrom(feedService.getFeedForUrl(feedUrl));
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
