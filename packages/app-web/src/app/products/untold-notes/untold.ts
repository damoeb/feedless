import { VerticalSpec } from '../../all-verticals';
import { GqlVertical } from '../../../generated/graphql';

export const untold: VerticalSpec = {
  id: 'untold',
  product: GqlVertical.UntoldNotes,
  titleHtml: '<strong>Un</strong><em>told</em>',
  domain: 'notes.feedless.org',
  pageTitle: 'Untold Notes',
  title: 'Untold Notes',
  offlineSupport: true,
  version: [0, 1, 0],
  phase: 'development',
  subtitle: 'Note App',
  summary: 'Minimalistic, Searchable and Linkable markdown notes the Zettelkasten way',
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
};
