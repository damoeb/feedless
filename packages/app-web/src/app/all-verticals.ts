import { GqlVertical } from '../generated/graphql';
import { upcomingVerticals } from './products/upcoming/upcoming';
import { feedless } from './products/feedless/feedless';
import { untold } from './products/untold-notes/untold';
import { reader } from './products/reader/reader';
import { visualDiff } from './products/visual-diff/visual-diff';
import { rssBuilder } from './products/rss-builder/rss-builder';

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
    rssBuilder,
    visualDiff,
    reader,
    untold,
    // {
    //   id: 'digest',
    //   product: GqlProductCategory.Digest,
    //   titleHtml: '<strong>Digest</strong><em>this</em>',
    //   pageTitle: 'Mail Digest',
    //   title: 'Mail Digest',
    //   offlineSupport: false,
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
    feedless,
    upcomingVerticals,
    // {
    //   id: 'changeTracker',
    //   product: GqlProductCategory.PageChangeTracker,
    //   title: 'Page Change Tracker',
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
