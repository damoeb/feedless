import { TranslationsLanguage } from './app/types';

export const translations: TranslationsLanguage[] = [
  {
    lang: 'de',
    translations: {
      'upcoming.perimeter': 'Umkreis',
      'upcoming.location': 'Deine aktuelle Position',
      'upcoming.today': 'Heute',
      'upcoming.map': 'Karte',
      'upcoming.typeZipOrPlace': 'Tippe deine PLZ oder Ortschaft',
      'upcoming.categories': 'Kategorien',
      'upcoming.no_events_found': 'Keine Einträge gefunden',
      'upcoming.place_distance': '{{distance}} Km entfernt',
      'upcoming.events': 'Veranstaltungen am {{date}} nahe {{place}}',
      'upcoming.type_location': 'Tippe deine PLZ oder Ortschaft',
      'upcoming.support': 'Unterst&uuml;tzung',
      'upcoming.imprint': 'Impressum',
      'upcoming.by': 'Entwickelt von <a href="mailto:">Markus Ruepp</a> basierend auf <a href="https://github.com/damoeb/feedless">feedless</a>',
      'upcoming.about': 'Upcoming ist ein nicht-kommerzielles Hobbyprojekt um unsere lokalen, kommunalen Strukturen zu stärken'
    }
  },
  {
    lang: 'en',
    translations: {
      'upcoming.perimeter': 'Perimeter',
      'upcoming.location': 'Your Location',
      'upcoming.today': 'Today',
      'upcoming.map': 'Map',
      'upcoming.typeZipOrPlace': 'Enter your zip or place',
      'upcoming.categories': 'Categories',
      'upcoming.no_events_found': 'No events found',
      'upcoming.place_distance': '{{distance}} Km distanced',
      'upcoming.events': 'Events on {{date}} near {{place}}',
      'upcoming.type_location': 'Type your postcode pr city',
      'upcoming.support': 'Support',
      'upcoming.imprint': 'Imprint',
      'upcoming.by': 'Developed by <a href="mailto:">Markus Ruepp</a> based on <a href="https://github.com/damoeb/feedless">feedless</a>',
      'upcoming.about': 'Upcoming is a non-commercial side project to strengthen our small local communal structures'
    }
  }
];
