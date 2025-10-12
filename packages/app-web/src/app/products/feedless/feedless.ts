import { GqlVertical } from '../../../generated/graphql';
import { VerticalSpec } from '../../all-verticals';

export const feedless: VerticalSpec = {
  id: 'feedless',
  product: GqlVertical.Feedless,
  domain: 'staging.feedless.org',
  title: 'Feedless',
  titleHtml: '<strong>feed</strong><em>less</em>',
  pageTitle: 'feedless',
  subtitle: 'All In One',
  version: [0, 7, 0],
  phase: 'development',
  summary: 'Build automated workflows visually or using code',
  descriptionMarkdown: `Time is precious, let's automate the web, build our custom well-behaving bots. _feedless_ is a general platform to
      build web-based workflows. Extendable using plugins. Popular solutions like zapier of ITTT steer into that direction.

Extraction
 * create feed from website
 * merge multiple feeds
 * use feed and filter title not includes 'Ad'
 * track pixel page changes of [url], but ship latest text and latest image
 * track text page changes of [url], but ship diff to first for 2 weeks
 * track price of product on [url] by extracting field, but shipping product fragment as pixel and markup
 * use existing feed -> readability, inline images and untrack urls
 * generate feed, fix title by removing prefix, trim after length 20
 * inbox: select feeds, filter last 24h, order by quality, pick best 12
 * digest: select feed, send best 10 end of week as digest via mail
 * create feed activate tracking
 * create just the document repository
`,
  videoUrl: '',
  links: [{ url: '/', allow: true }],
  features: ['Workflow Builder', 'Self-Hosting or SaaS'],
  localSetupBash: ``,
};
