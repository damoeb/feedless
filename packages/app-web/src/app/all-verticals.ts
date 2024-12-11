import { GqlVertical } from '../generated/graphql';

export type VerticalId =
  | 'changeTracker'
  | 'rss-proxy'
  | 'visual-diff'
  | 'reader'
  | 'upcoming'
  | 'digest'
  | 'untold'
  | 'feedless';

// export type AppStage = 'idea' | 'development' | 'alpha' | 'stable'

export type CostItem = {
  name: string;
  unit: string;
  value: {
    price?: number;
    discount?: number;
  };
};

export type AppLink = {
  url: string;
  allow: boolean;
};

export type VerticalSpec = {
  id: VerticalId;
  product: GqlVertical;
  domain?: string;
  localSetupBeforeMarkup?: string;
  localSetupBash: string;
  localSetupAfterMarkup?: string;
  title: string;
  titleHtml: string;
  pageTitle: string;
  offlineSupport?: boolean;
  subtitle: string;
  summary: string;
  // stage: AppStage;
  version?: (number | string)[];
  phase: 'planning' | 'design' | 'development' | 'alpha' | 'beta' | 'rc' | 'ga';
  descriptionMarkdown: string;
  descriptionHtml?: string;
  videoUrl?: string;
  costs?: CostItem[];
  links: AppLink[];
  listed: boolean;
  features: string[];
};

export type AllVerticals = {
  license: string;
  localSetup: string;
  verticals: VerticalSpec[];
};

export const allVerticals: AllVerticals = {
  license:
    'is released under non-competitive FSL license, that falls back to Open Source Apache 2 after two years ([FSL-1.0-Apache-2.0](https://fsl.software/)).',
  localSetup:
    'Once you have [docker-compose](https://docs.docker.com/compose/install/) or [podman-compose](https://docs.podman.io/en/latest/markdown/podman-compose.1.html), here is the basic setup.',
  verticals: [
    {
      id: 'rss-proxy',
      product: GqlVertical.RssProxy,
      title: 'RSS-proxy',
      titleHtml: '<strong>RSS</strong><em>Proxy</em>',
      pageTitle: 'RSS Proxy',
      domain: 'rss-proxy.migor.org',
      subtitle: 'RSS Feed Builder',
      version: [3, 0, 0, 'rc-1'],
      phase: 'rc',
      listed: true,
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
    },
    {
      id: 'visual-diff',
      product: GqlVertical.VisualDiff,
      titleHtml: '<strong>Visual</strong><em>Diff</em>',
      pageTitle: 'VisualDiff',
      title: 'VisualDiff',
      subtitle: 'Page Tracker',
      version: [0, 1, 0],
      listed: false,
      phase: 'alpha',
      summary: 'Detect changes in a website and get a notified',
      descriptionMarkdown:
        'Detect changes in a website based on image, markup or text and get a notification via mail or feed.',
      // costs: 79.99,
      videoUrl: 'https://www.youtube.com/watch?v=PolMYwBVmzc',
      links: [{ url: '/', allow: true }],
      features: [
        'Page change tracking',
        'Track pixel, markup or text',
        'Customizable trigger threshold',
        'Support full page and page fragment',
        'Prerendering in chromium',
        'Email notifications',
        'Self Hosting',
      ],
      localSetupBeforeMarkup: `It will start a database, feedless and a headless chromium serving the VisualDiff user interface.`,
      localSetupBash: `wget https://raw.githubusercontent.com/damoeb/visual-diff/master/selfhosting.env \\
  https://raw.githubusercontent.com/damoeb/visual-diff/master/docker-compose.yml \\
  https://raw.githubusercontent.com/damoeb/visual-diff/master/chrome.json

docker-compose up`,
      localSetupAfterMarkup: `Then, open http://localhost:8080 in your browser`,
    },
    {
      id: 'reader',
      product: GqlVertical.Reader,
      titleHtml: '<strong>Reader</strong>',
      pageTitle: 'Reader',
      title: 'Reader',
      subtitle: 'Reader Mode',
      phase: 'alpha',
      version: [0, 1, 0],
      listed: false,
      summary:
        'Unclutter a website and transform it into a version optimized for reading',
      descriptionMarkdown: `Unclutter a website and transform it into a version optimized for reading.
      Create an immersive reading experience without the dark patterns, advertisements and bad user interfaces.
      Inspiration for this comes from uncomissioned readability.com or instapaper`,
      // costs: 4.99,
      videoUrl: 'https://www.youtube.com/watch?v=PolMYwBVmzc',
      links: [{ url: '/', allow: true }],
      features: [
        'Font Family',
        'Text Size',
        'Text Alignment',
        'Bionic Font',
        'Self Hosting',
      ],
      localSetupBeforeMarkup: `It will just feedless.`,
      localSetupBash: `bash
wget https://raw.githubusercontent.com/damoeb/reader/master/selfhosting.env \\
  https://raw.githubusercontent.com/damoeb/reader/master/docker-compose.yml \\
  https://raw.githubusercontent.com/damoeb/reader/master/chrome.json

docker-compose up`,
      localSetupAfterMarkup: `Then, open http://localhost:8080 in your browser`,
    },
    {
      id: 'untold',
      product: GqlVertical.UntoldNotes,
      titleHtml: '<strong>Un</strong><em>told</em>',
      pageTitle: 'Untold Notes',
      title: 'Untold Notes',
      offlineSupport: true,
      listed: false,
      version: [0, 1, 0],
      phase: 'development',
      subtitle: 'Note App',
      summary:
        'Minimalistic, Searchable and Linkable markdown notes the Zettelkasten way',
      descriptionMarkdown: `Minimalistic, Searchable and Linkable markdown notes to facilitate
[Luhmann's Zettelkasten](https://en.wikipedia.org/wiki/Zettelkasten)
approach in the tradition of [Notational Velocity](https://en.wikipedia.org/wiki/Notational_Velocity)
and [The Archive](https://zettelkasten.de/the-archive/).
Untold Notes follows it's unopinionated approach and lifts it to an
open web platform.

[Nichlas Luhman](https://en.wikipedia.org/wiki/Niklas_Luhmann), the german sociologist, had a incredible productive scientific career, mainly attributed to
his approach to structure information in a Zettelkasten. His approach will help you to drastically improve your
way to think, learn and remember.`,
      // costs: -1,
      videoUrl: '',
      links: [{ url: '/', allow: true }],
      features: [
        'Text based notes',
        'Markdown editor (codemirror 6)',
        'Binary Attachments',
        'Self Hosting',
        'Offline support',
      ],
      localSetupBash: ``,
    },
    // {
    //   id: 'digest',
    //   product: GqlProductCategory.Digest,
    //   titleHtml: '<strong>Digest</strong><em>this</em>',
    //   pageTitle: 'Mail Digest',
    //   title: 'Mail Digest',
    //   offlineSupport: false,
    //   listed: false,
    //   phase: 'planning',
    //   subtitle: 'Digest Service',
    //   summary: 'Get the gist of your feeds or data streams.',
    //   descriptionMarkdown: `'If you don't have the capacity to stay on top of all
    //   potentially interesting information streams, _Mail Digest_ might be for you.
    //   Define your sources, you care about and when you want to receive the brief summary.`,
    //   videoUrl: '',
    //   features: [
    //     'Ranking',
    //     'Forward to multiple Email Addresses',
    //     'Self Hosting',
    //   ],
    //   localSetupBash: ``,
    // },
    {
      id: 'feedless',
      product: GqlVertical.Feedless,
      domain: 'feedless.org',
      title: 'Feedless',
      titleHtml: '<strong>feed</strong><em>less</em>',
      pageTitle: 'feedless',
      subtitle: 'All In One',
      version: [0, 7, 0],
      phase: 'development',
      listed: false,
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
    },
    {
      id: 'upcoming',
      product: GqlVertical.Upcoming,
      domain: 'lokale.events',
      title: 'lokale.events',
      titleHtml: '<strong>lokale</strong><em>events</em>',
      pageTitle: 'lokale.events',
      listed: false,
      subtitle: 'Localized event sourcing',
      summary: 'Searchable geo-located events sources from any sources',
      descriptionMarkdown: `There was a time when the social event calendar [yahoo upcoming](https://en.wikipedia.org/wiki/Upcoming) was
      popular, then social media coorps took over. Since a couple of years I am noticing that most local relevant events are non-commercial,
      therefore not listed on commercial ticket platforms, just shared on their particular website mostly relying on Word-of-mouth marketing.`,
      phase: 'development',
      videoUrl: '',
      links: [
        { url: '/', allow: true },
        { url: '/events/in/CH/ZH/Affoltern%2520am%2520Albis/', allow: true },
        { url: '/events/in/CH/ZH/Birmensdorf/', allow: true },
        { url: '/events/in/CH/ZH/R%25C3%25BCschlikon/', allow: true },
        { url: '/events/in/CH/ZH/Thalwil/', allow: true },
        { url: '/events/in/CH/AG/Wohlen%2520AG/', allow: true },
        { url: '/events/in/CH/ZH/Urdorf/', allow: true },
        { url: '/ueber-uns/', allow: true },
        { url: '/agb/', allow: true },
      ],
      features: [
        'Seed Events from Websites',
        'Source Localization',
        'Self-Hosting or SaaS',
      ],
      localSetupBash: ``,
    },
    // {
    //   id: 'changeTracker',
    //   product: GqlProductCategory.PageChangeTracker,
    //   title: 'Page Change Tracker',
    //   listed: false,
    //   titleHtml: '<strong>Page</strong><em>Change</em>',
    //   pageTitle: 'Page Change Tracker',
    //   subtitle: 'Track any change of a website',
    //   summary: '',
    //   descriptionMarkdown: `revisions of website`,
    //   phase: 'planning',
    //   videoUrl: '',
    //   features: ['Page Revisions', 'Self-Hosting or SaaS'],
    //   localSetupBash: ``,
    // },
  ],
};
