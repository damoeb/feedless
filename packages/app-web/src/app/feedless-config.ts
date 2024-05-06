import { GqlProductName } from '../generated/graphql';

export type ProductId =
  | 'pageChangeTracker'
  | 'rss-proxy'
  | 'visual-diff'
  | 'reader'
  | 'upcoming'
  | 'mail-digest'
  | 'untold'
  | 'feedless';

// export type AppStage = 'idea' | 'development' | 'alpha' | 'stable'

export type AppConfig = {
  id: ProductId;
  product: GqlProductName;
  localSetup: string;
  title: string;
  titleHtml: string;
  pageTitle: string;
  offlineSupport?: boolean;
  subtitle: string;
  summary: string;
  // stage: AppStage;
  isUnstable: boolean;
  descriptionMarkdown: string;
  descriptionHtml?: string;
  videoUrl: string;
  costs: number;
  features: string[];
};

export type FeedlessConfig = {
  license: string;
  localSetup: string;
  apps: AppConfig[];
};

export const feedlessConfig: FeedlessConfig = {
  license:
    'is released under non-competitive FSL license, that falls back to Open Source Apache 2 after two years ([FSL-1.0-Apache-2.0](https://fsl.software/)).',
  localSetup:
    'Once you have [docker-compose](https://docs.docker.com/compose/install/) or [podman-compose](https://docs.podman.io/en/latest/markdown/podman-compose.1.html), here is the basic setup.',
  apps: [
    {
      id: 'rss-proxy',
      product: GqlProductName.RssBuilder,
      title: 'RSS-proxy',
      titleHtml: '<strong>RSS</strong><em>Proxy</em>',
      pageTitle: 'RSS Proxy',
      subtitle: 'RSS Feed Builder',
      isUnstable: false,
      summary: 'Create feeds from Websites',
      descriptionMarkdown: `RSS-proxy allows you to do create an ATOM or JSON feed of any static website or feeds (web to feed),
just by analyzing just the HTML structure. Usually the structuring to a feed works automatically.`,
      costs: 29.99,
      videoUrl: 'https://www.youtube.com/watch?v=PolMYwBVmzc',
      features: [
        'Web to Feed',
        '[Filters](https://github.com/damoeb/feedless/blob/master/docs/filters.md)',
        'Custom feed parser rules',
        'Pre-rendering in chromium',
        'Self Hosting',
      ],
      localSetup: `It will start a database and feedless serving the RSS Proxy user interface.
\`\`\`bash
wget https://raw.githubusercontent.com/damoeb/rss-proxy/master/selfhosting.env \\
  https://raw.githubusercontent.com/damoeb/rss-proxy/master/docker-compose.yml \\
  https://raw.githubusercontent.com/damoeb/rss-proxy/master/chrome.json

docker-compose up
\`\`\`

Then, open http://localhost:8080 in your browser`,
    },
    {
      id: 'visual-diff',
      product: GqlProductName.VisualDiff,
      titleHtml: '<strong>Visual</strong><em>Diff</em>',
      pageTitle: 'VisualDiff',
      title: 'VisualDiff',
      subtitle: 'Page Change Tracker',
      isUnstable: false,
      summary: 'Detect changes in a website and get a notified',
      descriptionMarkdown:
        'Detect changes in a website based on image, markup or text and get a notification via mail or feed.',
      costs: 79.99,
      videoUrl: 'https://www.youtube.com/watch?v=PolMYwBVmzc',
      features: [
        'Page change tracking',
        'Track pixel, markup or text',
        'Customizable trigger threshold',
        'Support full page and page fragment',
        'Prerendering in chromium',
        'Email notifications',
        'Self Hosting',
      ],
      localSetup: `It will start a database, feedless and a headless chromium serving the VisualDiff user interface.
\`\`\`bash
wget https://raw.githubusercontent.com/damoeb/visual-diff/master/selfhosting.env \\
  https://raw.githubusercontent.com/damoeb/visual-diff/master/docker-compose.yml \\
  https://raw.githubusercontent.com/damoeb/visual-diff/master/chrome.json

docker-compose up
\`\`\`

Then, open http://localhost:8080 in your browser`,
    },
    {
      id: 'reader',
      product: GqlProductName.Reader,
      titleHtml: '<strong>Reader</strong>',
      pageTitle: 'Reader',
      title: 'Reader',
      subtitle: 'Reader Mode',
      isUnstable: false,
      summary:
        'Unclutter a website and transform it into a version optimized for reading',
      descriptionMarkdown: `Unclutter a website and transform it into a version optimized for reading.
      Create an immersive reading experience without the dark patterns, advertisements and bad user interfaces.
      Inspiration for this comes from uncomissioned readability.com or instapaper`,
      costs: 4.99,
      videoUrl: 'https://www.youtube.com/watch?v=PolMYwBVmzc',
      features: [
        'Font Family',
        'Text Size',
        'Text Alignment',
        'Bionic Font',
        'Self Hosting',
      ],
      localSetup: `It will just feedless.
\`\`\`bash
wget https://raw.githubusercontent.com/damoeb/reader/master/selfhosting.env \\
  https://raw.githubusercontent.com/damoeb/reader/master/docker-compose.yml \\
  https://raw.githubusercontent.com/damoeb/reader/master/chrome.json

docker-compose up
\`\`\`

Then, open http://localhost:8080 in your browser
`,
    },
    {
      id: 'untold',
      product: GqlProductName.UntoldNotes,
      titleHtml: '<strong>Un</strong><em>told</em>',
      pageTitle: 'Untold Notes',
      title: 'Untold Notes',
      offlineSupport: true,
      isUnstable: true,
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
      costs: -1,
      videoUrl: '',
      features: [
        'Text based notes',
        'Markdown editor (codemirror 6)',
        'Binary Attachments',
        'Self Hosting',
        'Offline support',
      ],
      localSetup: ``,
    },
    {
      id: 'mail-digest',
      product: GqlProductName.Digest,
      titleHtml: '<strong>mail</strong><em>digest</em>',
      pageTitle: 'Mail Digest',
      title: 'Mail Digest',
      offlineSupport: false,
      isUnstable: true,
      subtitle: 'Digest Service',
      summary: 'Get the gist of your feeds or data streams.',
      descriptionMarkdown: `'If you don't have the capacity to stay on top of all
      potentially interesting information streams, _Mail Digest_ might be for you.
      Define your sources, you care about and when you want to receive the brief summary.`,
      costs: -1,
      videoUrl: '',
      features: [
        'Aggregate',
        'Filter & Refine',
        'Ranking',
        'Self Hosting',
        'Forward to multiple Email Addresses',
        'Self Hosting',
      ],
      localSetup: ``,
    },
    {
      id: 'feedless',
      product: GqlProductName.Feedless,
      title: 'Feedless',
      titleHtml: '<strong>feed</strong><em>less</em>',
      pageTitle: 'feedless',
      subtitle: 'Workflow builder',
      isUnstable: true,
      summary: 'Build automated worflows visually or using code',
      descriptionMarkdown: `It's quite astoinding that the web is so hard to automate, the word _bot_ mainly has a negative connotation. I believe the opposite.
There is a dual use and everyone should have their well-behaving bots roaming the web in their interest.
Popular solutions like zapier of ITTT steer into that direction.`,
      costs: -1,
      videoUrl: '',
      features: ['Workflow Builder', 'Self-Hosting or SaaS'],
      localSetup: ``,
    },
    {
      id: 'upcoming',
      product: GqlProductName.Upcoming,
      title: 'Upcoming',
      titleHtml: '<strong>Up</strong><em>coming</em>',
      pageTitle: 'Upcoming',
      subtitle: 'Localized event sourcing',
      summary: 'Searchable geo-located events sources from any sources',
      descriptionMarkdown: `There was a time when the social event calendar [yahoo upcoming](https://en.wikipedia.org/wiki/Upcoming) was
      popular, then social media coorps took over. Since a couple of years I am noticing that most local relevant events are non-commercial,
      therefore not listed on commercial ticket platforms, just shared on their particular website mostly relying on Word-of-mouth marketing.`,
      costs: -1,
      isUnstable: true,
      videoUrl: '',
      features: [
        'Seed Events from Websites',
        'Source Localization',
        'Self-Hosting or SaaS',
      ],
      localSetup: ``,
    },
    {
      id: 'pageChangeTracker',
      product: GqlProductName.PageChangeTracker,
      title: 'Page Change Tracker',
      titleHtml: '<strong>Page</strong><em>Change</em>',
      pageTitle: 'Page Change Tracker',
      subtitle: 'Track any change of a website',
      summary: 'Searchable geo-located events sources from any sources',
      descriptionMarkdown: `There was a time when the social event calendar [yahoo upcoming](https://en.wikipedia.org/wiki/Upcoming) was
      popular, then social media coorps took over. Since a couple of years I am noticing that most local relevant events are non-commercial,
      therefore not listed on commercial ticket platforms, just shared on their particular website mostly relying on Word-of-mouth marketing.`,
      costs: -1,
      isUnstable: true,
      videoUrl: '',
      features: ['', 'Self-Hosting or SaaS'],
      localSetup: ``,
    },
    // {
    //   id: 'feedArchive',
    //   product: GqlProductName.FeedDump,
    //   title: 'Upcoming',
    //   titleHtml: '<strong>Up</strong><em>coming</em>',
    //   pageTitle: 'Upcoming',
    //   subtitle: 'Localized event sourcing',
    //   summary: 'Searchable geo-located events sources from any sources',
    //   descriptionMarkdown: `There was a time when the social event calendar [yahoo upcoming](https://en.wikipedia.org/wiki/Upcoming) was
    //   popular, then social media coorps took over. Since a couple of years I am noticing that most local relevant events are non-commercial,
    //   therefore not listed on commercial ticket platforms, just shared on their particular website mostly relying on Word-of-mouth marketing.`,
    //   costs: -1,
    //   isUnstable: true,
    //   videoUrl: '',
    //   features: [
    //     'Seed Events from Websites',
    //     'Source Localization',
    //     'Self-Hosting or SaaS'
    //   ],
    //   localSetup: ``,
    // }
  ],
};
