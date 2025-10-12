import { VerticalSpec } from '../../all-verticals';
import { GqlVertical } from '../../../generated/graphql';

export const upcomingVerticals: VerticalSpec = {
  id: 'upcoming',
  product: GqlVertical.Upcoming,
  domain: 'lokale.events',
  title: 'lokale.events',
  titleHtml: '<strong>lokale</strong><em>events</em>',
  pageTitle: 'lokale.events',
  subtitle: 'Localized event sourcing',
  summary:
    'Entdecke lokale Veranstaltungen und Events in der Schweiz - von Familien-Events bis zu kulturellen Veranstaltungen',
  descriptionMarkdown: `lokale.events ist deine Plattform für lokale Veranstaltungen in der Schweiz. Entdecke Events in deiner Nähe - von Familien-Aktivitäten über Sport-Events bis hin zu kulturellen Veranstaltungen, Märkten und Nachbarschaftsfesten.

Früher war der soziale Event-Kalender [Yahoo Upcoming](https://en.wikipedia.org/wiki/Upcoming) sehr beliebt, dann übernahmen Social Media Konzerne. Seit einigen Jahren bemerke ich, dass die meisten lokal relevanten Events nicht-kommerziell sind und daher nicht auf kommerziellen Ticket-Plattformen gelistet werden, sondern nur auf ihren eigenen Websites geteilt werden - hauptsächlich durch Mundpropaganda.

lokale.events sammelt diese versteckten Schätze und macht sie für alle sichtbar und durchsuchbar.`,
  phase: 'development',
  videoUrl: '',
  links: [
    { url: '/', allow: true },
    // Main event pages by location
    { url: '/events/in/CH/ZH/Affoltern%2520am%2520Albis/', allow: true },
    { url: '/events/in/CH/ZH/Birmensdorf/', allow: true },
    { url: '/events/in/CH/ZH/R%25C3%25BCschlikon/', allow: true },
    { url: '/events/in/CH/ZH/Thalwil/', allow: true },
    { url: '/events/in/CH/AG/Wohlen%2520AG/', allow: true },
    { url: '/events/in/CH/ZH/Urdorf/', allow: true },
    // Static pages
    { url: '/ueber-uns/', allow: true },
    { url: '/agb/', allow: true },
    // Admin areas - disallowed
    { url: '/management/', allow: false },
    { url: '/login', allow: false },
  ],
  features: ['Seed Events from Websites', 'Source Localization', 'Self-Hosting or SaaS'],
  localSetupBash: ``,
};
