import { GqlVertical } from '../../../generated/graphql';
import { VerticalSpec } from '../../all-verticals';

export const changeTracker: VerticalSpec = {
  id: 'changeTracker',
  product: GqlVertical.PageChangeTracker,
  title: 'Page Change Tracker',
  titleHtml: '<strong>Page</strong><em>Change</em>',
  pageTitle: 'Page Change Tracker',
  subtitle: 'Track any change of a website',
  summary: '',
  descriptionMarkdown: `revisions of website`,
  phase: 'planning',
  videoUrl: '',
  features: ['Page Revisions', 'Self-Hosting or SaaS'],
  localSetupBash: ``,
  links: [],
};
