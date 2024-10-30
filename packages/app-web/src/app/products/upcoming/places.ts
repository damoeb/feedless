export type NamedLatLon = {
  lat: number;
  lon: number;
  name: string;
};

export const namedPlaces: NamedLatLon[] = [
  {
    lat: 47.2607035,
    lon: 8.3732781,
    name: 'Merenschwand'
  },
  {
    lat: 47.2291056,
    lon: 8.3886537,
    name: 'Mühlau'
  },
  {
    lat: 47.2863467,
    lon: 8.3626162,
    name: 'Aristau'
  },
  {
    lat: 47.2617701,
    lon: 8.4197059,
    name: 'Obfelden'
  },
  {
    lat: 47.2679489,
    lon: 8.4863301,
    name: 'Aeugst am Albis'
  },
  {
    lat: 47.3390834,
    lon: 8.4734523,
    name: 'Wettswil am Albis'
  },
  {
    lat: 47.2349836,
    lon: 8.4266177,
    name: 'Maschwanden'
  },
  {
    lat: 47.3483234,
    lon: 8.3580094,
    name: 'Zufikon'
  },
  {
    lat: 47.3191633,
    lon: 8.4200738,
    name: 'Arni (AG)'
  },
  {
    lat: 47.2984412,
    lon: 8.4489755,
    name: 'Hedingen'
  },
  {
    lat: 47.311931,
    lon: 8.3904974,
    name: 'Oberlunkhofen'
  },
  {
    lat: 47.3222122,
    lon: 8.3805098,
    name: 'Unterlunkhofen'
  },
  {
    lat: 47.28172,
    lon: 8.4060915,
    name: 'Ottenbach'
  },
  {
    lat: 47.2251463,
    lon: 8.4573072,
    name: 'Knonau'
  },
  {
    lat: 47.3269232,
    lon: 8.4901051,
    name: 'Stallikon'
  },
  {
    lat: 47.3154662,
    lon: 8.4670602,
    name: 'Bonstetten'
  },
  {
    lat: 47.3539569,
    lon: 8.3376446,
    name: 'Bremgarten'
  },
  {
    lat: 47.3538481,
    lon: 8.4382157,
    name: 'Birmensdorf (ZH)'
  },
  {
    lat: 47.243488,
    lon: 8.4636702,
    name: 'Mettmenstetten'
  },
  {
    lat: 47.2438769,
    lon: 8.534235,
    name: 'Hausen am Albis'
  },
  {
    lat: 47.3504945,
    lon: 8.2786906,
    name: 'Wohlen'
  },
  {
    lat: 47.3004361,
    lon: 8.3137505,
    name: 'Boswil'
  },
  {
    lat: 47.2782467,
    lon: 8.4521518,
    name: 'Affoltern a.A.'
  },
  {
    lat: 47.3763559,
    lon: 8.2947614,
    name: 'Niederwil'
  }
];

export const countries = [
  {
    name: 'Zürich',
    places: [
      'Adliswil',
      'Aesch (ZH)',
      'Aeugst am Albis',
      'Affoltern am Albis',
      'Altikon',
      'Andelfingen ZH',
      'Bachenbülach',
      'Bachs',
      'Bäretswil',
      'Bassersdorf',
      'Bauma',
      'Benken (ZH)',
      'Berg am Irchel',
      'Birmensdorf ZH',
      'Bonstetten',
      'Boppelsen',
      'Brütten',
      'Bubikon',
      'Buch am Irchel',
      'Buchs ZH',
      'Bülach',
      'Dachsen',
      'Dägerlen',
      'Dällikon',
      'Dänikon',
      'Dättlikon',
      'Dielsdorf',
      'Dietikon',
      'Dietlikon',
      'Dinhard',
      'Dorf ZH',
      'Dübendorf',
      'Dürnten',
      'Egg ZH',
      'Eglisau',
      'Elgg',
      'Ellikon an der Thur',
      'Elsau',
      'Embrach',
      'Erlenbach ZH',
      'Faellanden',
      'Fehraltorf',
      'Feuerthalen',
      'Fischenthal',
      'Flaach',
      'Flurlingen',
      'Freienstein-Teufen',
      'Geroldswil',
      'Glattfelden',
      'Gossau ZH',
      'Greifensee',
      'Grüningen ZH',
      'Hagenbuch ZH',
      'Hausen am Albis',
      'Hedingen',
      'Henggart',
      'Herrliberg',
      'Hettlingen ZH',
      'Hinwil',
      'Hittnau',
      'Hochfelden',
      'Hombrechtikon',
      'Horgen',
      'Höri',
      'Hüntwangen',
      'Hüttikon',
      'Illnau-Effretikon',
      'Kappel am Albis',
      'Kilchberg ZH',
      'Kleinandelfingen',
      'Kloten',
      'Knonau',
      'Küsnacht ZH',
      'Langnau am Albis',
      'Laufen-Uhwiesen',
      'Lindau',
      'Lufingen',
      'Männedorf',
      'Marthalen',
      'Maschwanden',
      'Maur',
      'Meilen',
      'Mettmenstetten',
      'Mönchaltorf',
      'Neerach',
      'Neftenbach',
      'Niederglatt',
      'Niederhasli',
      'Niederweningen',
      'Nürensdorf',
      'Oberembrach',
      'Oberengstringen',
      'Oberglatt',
      'Oberrieden ZH',
      'Oberweningen',
      'Obfelden',
      'Oetwil am See',
      'Oetwil an der Limmat',
      'Opfikon',
      'Ossingen',
      'Otelfingen',
      'Ottenbach',
      'Pfäffikon ZH',
      'Pfungen',
      'Rafz',
      'Regensberg',
      'Regensdorf',
      'Rheinau ZH',
      'Richterswil',
      'Rickenbach ZH',
      'Rifferswil',
      'Rorbas',
      'Rümlang',
      'Rüschlikon',
      'Russikon',
      'Rüti ZH',
      'Schlatt ZH',
      'Schleinikon',
      'Schlieren',
      'Schöfflisdorf',
      'Schwerzenbach',
      'Seegräben',
      'Seuzach',
      'Stadel',
      'Stammheim',
      'Stäfa',
      'Stallikon',
      'Steinmaur',
      'Thalheim an der Thur',
      'Thalwil',
      'Trüllikon',
      'Truttikon',
      'Turbenthal',
      'Uetikon am See',
      'Uitikon',
      'Unterengstringen',
      'Urdorf',
      'Uster',
      'Volken',
      'Volketswil',
      'Wädenswil',
      'Wald ZH',
      'Wallisellen',
      'Wangen-Brüttisellen',
      'Wasterkingen',
      'Weiach',
      'Weiningen ZH',
      'Weisslingen',
      'Wettswil am Albis',
      'Wetzikon',
      'Wiesendangen',
      'Wil ZH',
      'Wila',
      'Wildberg',
      'Winkel',
      'Winterthur',
      'Zell ZH',
      'Zollikon',
      'Zumikon',
      'Zürich'
    ]
  },
  {
    name: 'Aargau',
    places: [
      'Aarau',
      'Aarburg',
      'Abtwil',
      'Ammerswil',
      'Aristau',
      'Arni (AG)',
      'Auenstein',
      'Auw',
      'Baden',
      'Beinwil (Freiamt)',
      'Beinwil am See',
      'Bellikon',
      'Bergdietikon',
      'Berikon',
      'Besenbüren',
      'Bettwil',
      'Biberstein',
      'Birmenstorf',
      'Birr',
      'Birrhard',
      'Birrwil',
      'Boniswil',
      'Boswil',
      'Bottenwil',
      'Böttstein',
      'Bözberg',
      'Böztal',
      'Bremgarten',
      'Brittnau',
      'Brugg',
      'Brunegg',
      'Buchs',
      'Bünzen',
      'Büttikon',
      'Buttwil',
      'Densbüren',
      'Dietwil',
      'Dintikon',
      'Dottikon',
      'Döttingen',
      'Dürrenäsch',
      'Eggenwil',
      'Egliswil',
      'Ehrendingen',
      'Eiken',
      'Endingen',
      'Ennetbaden',
      'Erlinsbach',
      'Fahrwangen',
      'Fischbach-Göslikon',
      'Fisibach',
      'Fislisbach',
      'Freienwil',
      'Frick',
      'Full-Reuenthal',
      'Gansingen',
      'Gebenstorf',
      'Geltwil',
      'Gipf-Oberfrick',
      'Gontenschwil',
      'Gränichen',
      'Habsburg',
      'Aarburg',
      'Hallwil',
      'Hausen',
      'Hellikon',
      'Hendschiken',
      'Herznach-Ueken',
      'Hirschthal',
      'Holderbank',
      'Holziken',
      'Hunzenschwil',
      'Islisberg',
      'Jonen',
      'Kaiseraugst',
      'Kaisten',
      'Kallern',
      'Killwangen',
      'Kirchleerau',
      'Klingnau',
      'Koblenz',
      'Kölliken',
      'Künten',
      'Küttigen',
      'Laufenburg',
      'Leibstadt',
      'Leimbach',
      'Lengnau',
      'Lenzburg',
      'Leuggern',
      'Leutwil',
      'Lupfig',
      'Magden',
      'Mägenwil',
      'Mandach',
      'Meisterschwanden',
      'Mellikon',
      'Mellingen',
      'Menziken',
      'Merenschwand',
      'Mettauertal',
      'Möhlin',
      'Mönthal',
      'Moosleerau',
      'Möriken-Wildegg',
      'Muhen',
      'Mühlau',
      'Mülligen',
      'Mumpf',
      'Münchwilen',
      'Murgenthal',
      'Muri',
      'Neuenhof',
      'Niederlenz',
      'Niederrohrdorf',
      'Niederwil',
      'Oberentfelden',
      'Oberhof',
      'Oberkulm',
      'Oberlunkhofen',
      'Obermumpf',
      'Oberrohrdorf',
      'Oberrüti',
      'Obersiggenthal',
      'Oberwil-Lieli',
      'Oeschgen',
      'Oftringen',
      'Olsberg',
      'Othmarsingen',
      'Reinach',
      'Reitnau',
      'Remetschwil',
      'Remigen',
      'Rheinfelden',
      'Riniken',
      'Rothrist',
      'Rottenschwil',
      'Rudolfstetten-Friedlisberg',
      'Rüfenach',
      'Rupperswil',
      'Safenwil',
      'Sarmenstorf',
      'Schafisheim',
      'Schinznach',
      'Schlossrued',
      'Schmiedrued',
      'Schneisingen',
      'Schöftland',
      'Schupfart',
      'Schwaderloch',
      'Seengen',
      'Seon',
      'Siglistorf',
      'Sins',
      'Sisseln',
      'Spreitenbach',
      'Staffelbach',
      'Staufen',
      'Stein',
      'Stetten',
      'Strengelbach',
      'Suhr',
      'Tägerig',
      'Tegerfelden',
      'Teufenthal',
      'Thalheim',
      'Uerkheim',
      'Uezwil',
      'Unterentfelden',
      'Unterkulm',
      'Unterlunkhofen',
      'Untersiggenthal',
      'Veltheim',
      'Villigen',
      'Villmergen',
      'Villnachern',
      'Vordemwald',
      'Wallbach',
      'Waltenschwil',
      'Wegenstetten',
      'Wettingen',
      'Widen',
      'Wiliberg',
      'Windisch',
      'Wittnau',
      'Wohlen',
      'Wohlenschwil',
      'Wölflinswil',
      'Würenlingen',
      'Würenlos',
      'Zeihen',
      'Aarburg',
      'Zetzwil',
      'Zofingen',
      'Zufikon',
      'Zurzach',
      'Zuzgen'
    ]
  }
];