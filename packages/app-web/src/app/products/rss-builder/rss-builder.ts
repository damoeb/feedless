import { VerticalSpec } from '../../all-verticals';
import { GqlVertical } from '../../../generated/graphql';

export const rssBuilder: VerticalSpec = {
  id: 'rss-proxy',
  product: GqlVertical.RssProxy,
  title: 'RSS-proxy',
  titleHtml: '<strong>RSS</strong><em>Proxy</em>',
  pageTitle: 'RSS Proxy',
  domain: 'rssproxy.feedless.org',
  subtitle: 'RSS Feed Builder',
  version: [3, 0, 0, 'rc-1'],
  phase: 'rc',
  summary: 'Create feeds from Websites',
  descriptionMarkdown: `RSS-proxy allows you to do create an ATOM or JSON feed of any static website or feeds (web to feed),
just by analyzing just the HTML structure. Usually the structuring to a feed works automatically.`,
  links: [{ url: '/', allow: true }],
  costs: [
    {
      name: 'First major release',
      unit: 'Release',
      value: {
        price: 49.99,
      },
    },
    {
      name: 'Second consecutive releases',
      unit: 'Release',
      value: {
        discount: 30,
      },
    },
    {
      name: 'Third consecutive releases onwards',
      unit: 'Release',
      value: {
        discount: 50,
      },
    },
  ],
  videoUrl: 'https://www.youtube.com/watch?v=7weraU_FpUs',
  features: [
    'Web to Feed',
    'Filters',
    'Fulltext Feed',
    'Privacy Plugins',
    'Custom feed parser rules',
    'JavaScript Support using chromium',
    'Self Hosting',
  ],
  localSetupBeforeMarkup: `It will start a database and feedless serving the RSS Proxy user interface.`,
  localSetupBash: `git clone --depth=1 https://github.com/damoeb/rss-proxy.git
cd rss-proxy
touch your-license.key
docker-compose up`,
  localSetupAfterMarkup: `Then, open http://localhost:8080 in your browser`,
};
