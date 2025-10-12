import { GqlVertical } from '../../../generated/graphql';
import { VerticalSpec } from '../../all-verticals';

export const reader: VerticalSpec = {
  id: 'reader',
  product: GqlVertical.Reader,
  titleHtml: '<strong>Reader</strong>',
  pageTitle: 'Reader',
  title: 'Reader',
  subtitle: 'Reader Mode',
  phase: 'alpha',
  version: [0, 1, 0],
  summary: 'Unclutter a website and transform it into a version optimized for reading',
  descriptionMarkdown: `Unclutter a website and transform it into a version optimized for reading.
      Create an immersive reading experience without the dark patterns, advertisements and bad user interfaces.
      Inspiration for this comes from uncomissioned readability.com or instapaper`,
  // costs: 4.99,
  videoUrl: 'https://www.youtube.com/watch?v=PolMYwBVmzc',
  links: [{ url: '/', allow: true }],
  features: ['Font Family', 'Text Size', 'Text Alignment', 'Bionic Font', 'Self Hosting'],
  localSetupBeforeMarkup: `It will just feedless.`,
  localSetupBash: `bash
wget https://raw.githubusercontent.com/damoeb/reader/master/selfhosting.env \\
  https://raw.githubusercontent.com/damoeb/reader/master/docker-compose.yml \\
  https://raw.githubusercontent.com/damoeb/reader/master/chrome.json

docker-compose up`,
  localSetupAfterMarkup: `Then, open http://localhost:8080 in your browser`,
};
