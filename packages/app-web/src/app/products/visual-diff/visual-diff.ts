import { VerticalSpec } from '../../all-verticals';
import { GqlVertical } from '../../../generated/graphql';

export const visualDiff: VerticalSpec = {
  id: 'visual-diff',
  product: GqlVertical.VisualDiff,
  titleHtml: '<strong>Visual</strong><em>Diff</em>',
  pageTitle: 'VisualDiff',
  title: 'VisualDiff',
  subtitle: 'Page Tracker',
  version: [0, 1, 0],
  domain: 'visualdiff.feedless.org',
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
};
