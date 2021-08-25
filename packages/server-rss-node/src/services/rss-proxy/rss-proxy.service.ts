import { Injectable, Logger } from '@nestjs/common';
import { JSDOM } from 'jsdom';
import { Article, ArticleRule, FeedParser, OutputType } from '@rss-proxy/core';
import { Field, ObjectType } from 'type-graphql';

@ObjectType()
export class ProxyArticleRule {
  constructor(rule: ArticleRule) {
    this.score = rule.score;
    this.contextXPath = rule.contextXPath;
    this.linkXPath = rule.linkXPath;
    this.extendContext = rule.extendContext;
    this.id = rule.id;
  }

  @Field({ nullable: true })
  score?: number;
  @Field()
  linkXPath: string;
  @Field()
  extendContext: string;
  @Field()
  contextXPath: string;
  @Field()
  id: string;
}

@ObjectType()
export class ProxyArticle {
  constructor(article: Article) {
    this.title = article.title;
    this.link = article.link;
    this.summary = article.summary;
    this.content = article.content;
    this.text = article.text;
  }

  @Field()
  title: string;
  @Field()
  link: string;
  @Field(() => [String], { nullable: true })
  summary?: string[];
  @Field({ nullable: true })
  content?: string;
  @Field({ nullable: true })
  text?: string;
}

@ObjectType()
export class ProxyFeed {
  constructor(
    rule: ProxyArticleRule,
    articles: ProxyArticle[],
    feed_url: string,
    title: string,
    home_page_url: string,
  ) {
    this.rule = rule;
    this.articles = articles;
    this.title = title;
    this.feed_url = feed_url;
    this.home_page_url = home_page_url;
  }
  @Field(() => ProxyArticleRule)
  rule: ProxyArticleRule;
  @Field(() => [ProxyArticle])
  articles: ProxyArticle[];
  @Field()
  feed_url: string;
  @Field()
  home_page_url: string;
  @Field()
  title: string;
}

@ObjectType()
export class ProxyFeeds {
  @Field()
  home_page_url: string;
  @Field()
  html: string;
  @Field()
  title: string;
  @Field(() => [ProxyFeed])
  feeds: ProxyFeed[];
}

@Injectable()
export class RssProxyService {
  private readonly logger = new Logger(RssProxyService.name);

  async parseFeeds(url: string, html: string): Promise<ProxyFeeds> {
    try {
      const doc = new JSDOM(html).window.document;
      const parser = new FeedParser(
        doc,
        url,
        { o: OutputType.JSON },
        { log: () => null, error: () => null },
      );
      const rules = parser.getArticleRules();
      if (rules.length === 0) {
        throw new Error(`No rules found`);
      }
      const title = doc.title;
      const feeds = rules.map((rule) => {
        const articles = parser.getArticlesByRule(rule);
        return new ProxyFeed(
          new ProxyArticleRule(rule),
          articles.map((article) => new ProxyArticle(article)),
          this.constructUrl(url, rule),
          title,
          url,
        );
      });
      console.log('feeds', feeds.length);
      return { home_page_url: url, html, feeds, title };
    } catch (e) {
      this.logger.error(e.message);
      throw e;
    }
  }

  private constructUrl(url: string, rule: ArticleRule): string {
    const params = {
      linkXPath: rule.linkXPath,
      extendContext: rule.extendContext,
      contextXPath: rule.contextXPath,
      url,
    };
    const queryString = Object.keys(params)
      .map((key) => `${key}=${encodeURIComponent(params[key])}`)
      .join('&');
    return `http://localhost:8080/api/rss-proxy?${queryString}`;
  }
}
