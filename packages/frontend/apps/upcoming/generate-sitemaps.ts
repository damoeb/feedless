import { SitemapStream, streamToPromise } from 'sitemap';
import { writeFileSync } from 'fs';
import { join } from 'path';
import { Readable } from 'node:stream';

const places: { place: string; area: string }[] = [
  {
    place: 'Zug',
    area: 'ZG',
  },
  {
    place: 'Steinhausen',
    area: 'ZG',
  },
  {
    place: 'Neuägeri',
    area: 'ZG',
  },
  {
    place: 'Allenwinden',
    area: 'ZG',
  },
  {
    place: 'Baar',
    area: 'ZG',
  },
  {
    place: 'Hünenberg',
    area: 'ZG',
  },
  {
    place: 'Hagendorn',
    area: 'ZG',
  },
  {
    place: 'Mühlau',
    area: 'ZG',
  },
  {
    place: 'Hünenberg See',
    area: 'ZG',
  },
  {
    place: 'Menzingen',
    area: 'ZG',
  },
  {
    place: 'Edlibach',
    area: 'ZG',
  },
  {
    place: 'Finstersee',
    area: 'ZG',
  },
  {
    place: 'Neuheim',
    area: 'ZG',
  },
  {
    place: 'Alosen',
    area: 'ZG',
  },
  {
    place: 'Morgarten',
    area: 'ZG',
  },
  {
    place: 'Sattel',
    area: 'ZG',
  },
  {
    place: 'Rothenthurm',
    area: 'ZG',
  },
  {
    place: 'Bennau',
    area: 'ZG',
  },
  {
    place: 'Rotkreuz',
    area: 'ZG',
  },
  {
    place: 'Buonas',
    area: 'ZG',
  },
  {
    place: 'Risch',
    area: 'ZG',
  },
  {
    place: 'Holzhäusern ZG',
    area: 'ZG',
  },
  {
    place: 'Unterägeri',
    area: 'ZG',
  },
  {
    place: 'Oberägeri',
    area: 'ZG',
  },
  {
    place: 'Zugerberg',
    area: 'ZG',
  },
  {
    place: 'Oberwil b. Zug',
    area: 'ZG',
  },
  {
    place: 'Walchwil',
    area: 'ZG',
  },
  {
    place: 'Cham',
    area: 'ZG',
  },
  {
    place: 'Aarau',
    area: 'AG',
  },
  {
    place: 'Aarau Rohr',
    area: 'AG',
  },
  {
    place: 'Biberstein',
    area: 'AG',
  },
  {
    place: 'Buchs AG',
    area: 'AG',
  },
  {
    place: 'Asp',
    area: 'AG',
  },
  {
    place: 'Densbüren',
    area: 'AG',
  },
  {
    place: 'Barmelweid',
    area: 'AG',
  },
  {
    place: 'Erlinsbach',
    area: 'AG',
  },
  {
    place: 'Gränichen',
    area: 'AG',
  },
  {
    place: 'Teufenthal AG',
    area: 'AG',
  },
  {
    place: 'Hirschthal',
    area: 'AG',
  },
  {
    place: 'Rombach',
    area: 'AG',
  },
  {
    place: 'Küttigen',
    area: 'AG',
  },
  {
    place: 'Muhen',
    area: 'AG',
  },
  {
    place: 'Oberentfelden',
    area: 'AG',
  },
  {
    place: 'Suhr',
    area: 'AG',
  },
  {
    place: 'Unterentfelden',
    area: 'AG',
  },
  {
    place: 'Turgi',
    area: 'AG',
  },
  {
    place: 'Baden',
    area: 'AG',
  },
  {
    place: 'Dättwil AG',
    area: 'AG',
  },
  {
    place: 'Rütihof',
    area: 'AG',
  },
  {
    place: 'Bellikon',
    area: 'AG',
  },
  {
    place: 'Widen',
    area: 'AG',
  },
  {
    place: 'Bergdietikon',
    area: 'AG',
  },
  {
    place: 'Birmenstorf AG',
    area: 'AG',
  },
  {
    place: 'Ennetbaden',
    area: 'AG',
  },
  {
    place: 'Fislisbach',
    area: 'AG',
  },
  {
    place: 'Freienwil',
    area: 'AG',
  },
  {
    place: 'Gebenstorf',
    area: 'AG',
  },
  {
    place: 'Vogelsang AG',
    area: 'AG',
  },
  {
    place: 'Killwangen',
    area: 'AG',
  },
  {
    place: 'Künten',
    area: 'AG',
  },
  {
    place: 'Mägenwil',
    area: 'AG',
  },
  {
    place: 'Mellingen',
    area: 'AG',
  },
  {
    place: 'Neuenhof',
    area: 'AG',
  },
  {
    place: 'Niederrohrdorf',
    area: 'AG',
  },
  {
    place: 'Oberrohrdorf',
    area: 'AG',
  },
  {
    place: 'Nussbaumen AG',
    area: 'AG',
  },
  {
    place: 'Hertenstein AG',
    area: 'AG',
  },
  {
    place: 'Rieden AG',
    area: 'AG',
  },
  {
    place: 'Kirchdorf AG',
    area: 'AG',
  },
  {
    place: 'Remetschwil',
    area: 'AG',
  },
  {
    place: 'Dietikon',
    area: 'AG',
  },
  {
    place: 'Spreitenbach',
    area: 'AG',
  },
  {
    place: 'Stetten AG',
    area: 'AG',
  },
  {
    place: 'Siggenthal Station',
    area: 'AG',
  },
  {
    place: 'Untersiggenthal',
    area: 'AG',
  },
  {
    place: 'Wettingen',
    area: 'AG',
  },
  {
    place: 'Wohlenschwil',
    area: 'AG',
  },
  {
    place: 'Würenlingen',
    area: 'AG',
  },
  {
    place: 'Würenlos',
    area: 'AG',
  },
  {
    place: 'Kloster Fahr',
    area: 'AG',
  },
  {
    place: 'Ehrendingen',
    area: 'AG',
  },
  {
    place: 'Arni AG',
    area: 'AG',
  },
  {
    place: 'Rudolfstetten',
    area: 'AG',
  },
  {
    place: 'Berikon',
    area: 'AG',
  },
  {
    place: 'Bremgarten AG',
    area: 'AG',
  },
  {
    place: 'Hermetschwil-Staffeln',
    area: 'AG',
  },
  {
    place: 'Büttikon AG',
    area: 'AG',
  },
  {
    place: 'Hendschiken',
    area: 'AG',
  },
  {
    place: 'Dottikon',
    area: 'AG',
  },
  {
    place: 'Eggenwil',
    area: 'AG',
  },
  {
    place: 'Fischbach-Göslikon',
    area: 'AG',
  },
  {
    place: 'Tägerig',
    area: 'AG',
  },
  {
    place: 'Hägglingen',
    area: 'AG',
  },
  {
    place: 'Jonen',
    area: 'AG',
  },
  {
    place: 'Niederwil AG',
    area: 'AG',
  },
  {
    place: 'Nesselnbach',
    area: 'AG',
  },
  {
    place: 'Oberlunkhofen',
    area: 'AG',
  },
  {
    place: 'Oberwil-Lieli',
    area: 'AG',
  },
  {
    place: 'Sarmenstorf',
    area: 'AG',
  },
  {
    place: 'Bettwil',
    area: 'AG',
  },
  {
    place: 'Uezwil',
    area: 'AG',
  },
  {
    place: 'Unterlunkhofen',
    area: 'AG',
  },
  {
    place: 'Wohlen AG',
    area: 'AG',
  },
  {
    place: 'Anglikon',
    area: 'AG',
  },
  {
    place: 'Villmergen',
    area: 'AG',
  },
  {
    place: 'Hilfikon',
    area: 'AG',
  },
  {
    place: 'Zufikon',
    area: 'AG',
  },
  {
    place: 'Islisberg',
    area: 'AG',
  },
  {
    place: 'Auenstein',
    area: 'AG',
  },
  {
    place: 'Birr',
    area: 'AG',
  },
  {
    place: 'Birrhard',
    area: 'AG',
  },
  {
    place: 'Schinznach Bad',
    area: 'AG',
  },
  {
    place: 'Brugg AG',
    area: 'AG',
  },
  {
    place: 'Windisch',
    area: 'AG',
  },
  {
    place: 'Umiken',
    area: 'AG',
  },
  {
    place: 'Habsburg',
    area: 'AG',
  },
  {
    place: 'Hausen AG',
    area: 'AG',
  },
  {
    place: 'Lupfig',
    area: 'AG',
  },
  {
    place: 'Scherz',
    area: 'AG',
  },
  {
    place: 'Mandach',
    area: 'AG',
  },
  {
    place: 'Effingen',
    area: 'AG',
  },
  {
    place: 'Mönthal',
    area: 'AG',
  },
  {
    place: 'Mülligen',
    area: 'AG',
  },
  {
    place: 'Remigen',
    area: 'AG',
  },
  {
    place: 'Riniken',
    area: 'AG',
  },
  {
    place: 'Rüfenach AG',
    area: 'AG',
  },
  {
    place: 'Thalheim AG',
    area: 'AG',
  },
  {
    place: 'Veltheim AG',
    area: 'AG',
  },
  {
    place: 'Stilli',
    area: 'AG',
  },
  {
    place: 'Villigen',
    area: 'AG',
  },
  {
    place: 'Schinznach Dorf',
    area: 'AG',
  },
  {
    place: 'Villnachern',
    area: 'AG',
  },
  {
    place: 'Bözberg',
    area: 'AG',
  },
  {
    place: 'Oberflachs',
    area: 'AG',
  },
  {
    place: 'Beinwil am See',
    area: 'AG',
  },
  {
    place: 'Birrwil',
    area: 'AG',
  },
  {
    place: 'Dürrenäsch',
    area: 'AG',
  },
  {
    place: 'Unterkulm',
    area: 'AG',
  },
  {
    place: 'Schmiedrued',
    area: 'AG',
  },
  {
    place: 'Walde AG',
    area: 'AG',
  },
  {
    place: 'Gontenschwil',
    area: 'AG',
  },
  {
    place: 'Holziken',
    area: 'AG',
  },
  {
    place: 'Leimbach AG',
    area: 'AG',
  },
  {
    place: 'Boniswil',
    area: 'AG',
  },
  {
    place: 'Leutwil',
    area: 'AG',
  },
  {
    place: 'Burg AG',
    area: 'AG',
  },
  {
    place: 'Menziken',
    area: 'AG',
  },
  {
    place: 'Oberkulm',
    area: 'AG',
  },
  {
    place: 'Reinach AG',
    area: 'AG',
  },
  {
    place: 'Schlossrued',
    area: 'AG',
  },
  {
    place: 'Schöftland',
    area: 'AG',
  },
  {
    place: 'Zetzwil',
    area: 'AG',
  },
  {
    place: 'Eiken',
    area: 'AG',
  },
  {
    place: 'Frick',
    area: 'AG',
  },
  {
    place: 'Gansingen',
    area: 'AG',
  },
  {
    place: 'Wölflinswil',
    area: 'AG',
  },
  {
    place: 'Gipf-Oberfrick',
    area: 'AG',
  },
  {
    place: 'Laufenburg',
    area: 'AG',
  },
  {
    place: 'Kaisten',
    area: 'AG',
  },
  {
    place: 'Ittenthal',
    area: 'AG',
  },
  {
    place: 'Rheinsulz',
    area: 'AG',
  },
  {
    place: 'Sulz AG',
    area: 'AG',
  },
  {
    place: 'Münchwilen AG',
    area: 'AG',
  },
  {
    place: 'Oberhof',
    area: 'AG',
  },
  {
    place: 'Oeschgen',
    area: 'AG',
  },
  {
    place: 'Schwaderloch',
    area: 'AG',
  },
  {
    place: 'Sisseln AG',
    area: 'AG',
  },
  {
    place: 'Wittnau',
    area: 'AG',
  },
  {
    place: 'Zeihen',
    area: 'AG',
  },
  {
    place: 'Oberhofen AG',
    area: 'AG',
  },
  {
    place: 'Mettau',
    area: 'AG',
  },
  {
    place: 'Etzgen',
    area: 'AG',
  },
  {
    place: 'Wil AG',
    area: 'AG',
  },
  {
    place: 'Hottwil',
    area: 'AG',
  },
  {
    place: 'Hornussen',
    area: 'AG',
  },
  {
    place: 'Bözen',
    area: 'AG',
  },
  {
    place: 'Elfingen',
    area: 'AG',
  },
  {
    place: 'Herznach',
    area: 'AG',
  },
  {
    place: 'Ueken',
    area: 'AG',
  },
  {
    place: 'Ammerswil AG',
    area: 'AG',
  },
  {
    place: 'Brunegg',
    area: 'AG',
  },
  {
    place: 'Dintikon',
    area: 'AG',
  },
  {
    place: 'Seon',
    area: 'AG',
  },
  {
    place: 'Egliswil',
    area: 'AG',
  },
  {
    place: 'Fahrwangen',
    area: 'AG',
  },
  {
    place: 'Hallwil',
    area: 'AG',
  },
  {
    place: 'Holderbank AG',
    area: 'AG',
  },
  {
    place: 'Hunzenschwil',
    area: 'AG',
  },
  {
    place: 'Lenzburg',
    area: 'AG',
  },
  {
    place: 'Meisterschwanden',
    area: 'AG',
  },
  {
    place: 'Tennwil',
    area: 'AG',
  },
  {
    place: 'Wildegg',
    area: 'AG',
  },
  {
    place: 'Möriken AG',
    area: 'AG',
  },
  {
    place: 'Niederlenz',
    area: 'AG',
  },
  {
    place: 'Othmarsingen',
    area: 'AG',
  },
  {
    place: 'Rupperswil',
    area: 'AG',
  },
  {
    place: 'Schafisheim',
    area: 'AG',
  },
  {
    place: 'Seengen',
    area: 'AG',
  },
  {
    place: 'Staufen',
    area: 'AG',
  },
  {
    place: 'Abtwil AG',
    area: 'AG',
  },
  {
    place: 'Aristau',
    area: 'AG',
  },
  {
    place: 'Muri AG',
    area: 'AG',
  },
  {
    place: 'Beinwil (Freiamt)',
    area: 'AG',
  },
  {
    place: 'Mühlau',
    area: 'AG',
  },
  {
    place: 'Meienberg',
    area: 'AG',
  },
  {
    place: 'Auw',
    area: 'AG',
  },
  {
    place: 'Kleinwangen',
    area: 'AG',
  },
  {
    place: 'Besenbüren',
    area: 'AG',
  },
  {
    place: 'Buttwil',
    area: 'AG',
  },
  {
    place: 'Boswil',
    area: 'AG',
  },
  {
    place: 'Bünzen',
    area: 'AG',
  },
  {
    place: 'Waldhäusern AG',
    area: 'AG',
  },
  {
    place: 'Inwil',
    area: 'AG',
  },
  {
    place: 'Dietwil',
    area: 'AG',
  },
  {
    place: 'Geltwil',
    area: 'AG',
  },
  {
    place: 'Kallern',
    area: 'AG',
  },
  {
    place: 'Merenschwand',
    area: 'AG',
  },
  {
    place: 'Benzenschwil',
    area: 'AG',
  },
  {
    place: 'Oberrüti',
    area: 'AG',
  },
  {
    place: 'Rottenschwil',
    area: 'AG',
  },
  {
    place: 'Sins',
    area: 'AG',
  },
  {
    place: 'Alikon',
    area: 'AG',
  },
  {
    place: 'Aettenschwil',
    area: 'AG',
  },
  {
    place: 'Fenkrieden',
    area: 'AG',
  },
  {
    place: 'Ballwil',
    area: 'AG',
  },
  {
    place: 'Waltenschwil',
    area: 'AG',
  },
  {
    place: 'Hellikon',
    area: 'AG',
  },
  {
    place: 'Hemmiken',
    area: 'AG',
  },
  {
    place: 'Kaiseraugst',
    area: 'AG',
  },
  {
    place: 'Olsberg',
    area: 'AG',
  },
  {
    place: 'Magden',
    area: 'AG',
  },
  {
    place: 'Möhlin',
    area: 'AG',
  },
  {
    place: 'Mumpf',
    area: 'AG',
  },
  {
    place: 'Obermumpf',
    area: 'AG',
  },
  {
    place: 'Rheinfelden',
    area: 'AG',
  },
  {
    place: 'Schupfart',
    area: 'AG',
  },
  {
    place: 'Stein AG',
    area: 'AG',
  },
  {
    place: 'Wallbach',
    area: 'AG',
  },
  {
    place: 'Wegenstetten',
    area: 'AG',
  },
  {
    place: 'Zeiningen',
    area: 'AG',
  },
  {
    place: 'Zuzgen',
    area: 'AG',
  },
  {
    place: 'Olten',
    area: 'AG',
  },
  {
    place: 'Aarburg',
    area: 'AG',
  },
  {
    place: 'Oftringen',
    area: 'AG',
  },
  {
    place: 'Uerkheim',
    area: 'AG',
  },
  {
    place: 'Bottenwil',
    area: 'AG',
  },
  {
    place: 'Vordemwald',
    area: 'AG',
  },
  {
    place: 'Brittnau',
    area: 'AG',
  },
  {
    place: 'Pfaffnau',
    area: 'AG',
  },
  {
    place: 'Kirchleerau',
    area: 'AG',
  },
  {
    place: 'Kölliken',
    area: 'AG',
  },
  {
    place: 'Moosleerau',
    area: 'AG',
  },
  {
    place: 'Murgenthal',
    area: 'AG',
  },
  {
    place: 'Riken AG',
    area: 'AG',
  },
  {
    place: 'Glashütten',
    area: 'AG',
  },
  {
    place: 'Roggwil BE',
    area: 'AG',
  },
  {
    place: 'St. Urban',
    area: 'AG',
  },
  {
    place: 'Starrkirch-Wil',
    area: 'AG',
  },
  {
    place: 'Zofingen',
    area: 'AG',
  },
  {
    place: 'Attelwil',
    area: 'AG',
  },
  {
    place: 'Reitnau',
    area: 'AG',
  },
  {
    place: 'Strengelbach',
    area: 'AG',
  },
  {
    place: 'Rothrist',
    area: 'AG',
  },
  {
    place: 'Safenwil',
    area: 'AG',
  },
  {
    place: 'Staffelbach',
    area: 'AG',
  },
  {
    place: 'Wittwil',
    area: 'AG',
  },
  {
    place: 'Mühlethal',
    area: 'AG',
  },
  {
    place: 'Wiliberg',
    area: 'AG',
  },
  {
    place: 'Kleindöttingen',
    area: 'AG',
  },
  {
    place: 'Böttstein',
    area: 'AG',
  },
  {
    place: 'Döttingen',
    area: 'AG',
  },
  {
    place: 'Endingen',
    area: 'AG',
  },
  {
    place: 'Unterendingen',
    area: 'AG',
  },
  {
    place: 'Fisibach',
    area: 'AG',
  },
  {
    place: 'Full-Reuenthal',
    area: 'AG',
  },
  {
    place: 'Klingnau',
    area: 'AG',
  },
  {
    place: 'Koblenz',
    area: 'AG',
  },
  {
    place: 'Leibstadt',
    area: 'AG',
  },
  {
    place: 'Lengnau AG',
    area: 'AG',
  },
  {
    place: 'Leuggern',
    area: 'AG',
  },
  {
    place: 'Hettenschwil',
    area: 'AG',
  },
  {
    place: 'Mellikon',
    area: 'AG',
  },
  {
    place: 'Schneisingen',
    area: 'AG',
  },
  {
    place: 'Siglistorf',
    area: 'AG',
  },
  {
    place: 'Tegerfelden',
    area: 'AG',
  },
  {
    place: 'Rietheim',
    area: 'AG',
  },
  {
    place: 'Bad Zurzach',
    area: 'AG',
  },
  {
    place: 'Rekingen AG',
    area: 'AG',
  },
  {
    place: 'Baldingen',
    area: 'AG',
  },
  {
    place: 'Böbikon',
    area: 'AG',
  },
  {
    place: 'Wislikofen',
    area: 'AG',
  },
  {
    place: 'Rümikon AG',
    area: 'AG',
  },
  {
    place: 'Kaiserstuhl AG',
    area: 'AG',
  },
  {
    place: 'Bennau',
    area: 'SZ',
  },
  {
    place: 'Einsiedeln',
    area: 'SZ',
  },
  {
    place: 'Trachslau',
    area: 'SZ',
  },
  {
    place: 'Gross',
    area: 'SZ',
  },
  {
    place: 'Euthal',
    area: 'SZ',
  },
  {
    place: 'Willerzell',
    area: 'SZ',
  },
  {
    place: 'Egg SZ',
    area: 'SZ',
  },
  {
    place: 'Rigi Scheidegg',
    area: 'SZ',
  },
  {
    place: 'Gersau',
    area: 'SZ',
  },
  {
    place: 'Wollerau',
    area: 'SZ',
  },
  {
    place: 'Schindellegi',
    area: 'SZ',
  },
  {
    place: 'Feusisberg',
    area: 'SZ',
  },
  {
    place: 'Hurden',
    area: 'SZ',
  },
  {
    place: 'Bäch SZ',
    area: 'SZ',
  },
  {
    place: 'Freienbach',
    area: 'SZ',
  },
  {
    place: 'Pfäffikon SZ',
    area: 'SZ',
  },
  {
    place: 'Wilen b. Wollerau',
    area: 'SZ',
  },
  {
    place: 'Samstagern',
    area: 'SZ',
  },
  {
    place: 'Merlischachen',
    area: 'SZ',
  },
  {
    place: 'Küssnacht am Rigi',
    area: 'SZ',
  },
  {
    place: 'Immensee',
    area: 'SZ',
  },
  {
    place: 'Altendorf',
    area: 'SZ',
  },
  {
    place: 'Galgenen',
    area: 'SZ',
  },
  {
    place: 'Innerthal',
    area: 'SZ',
  },
  {
    place: 'Reichenburg',
    area: 'SZ',
  },
  {
    place: 'Wangen SZ',
    area: 'SZ',
  },
  {
    place: 'Schübelbach',
    area: 'SZ',
  },
  {
    place: 'Buttikon SZ',
    area: 'SZ',
  },
  {
    place: 'Uznach',
    area: 'SZ',
  },
  {
    place: 'Tuggen',
    area: 'SZ',
  },
  {
    place: 'Vorderthal',
    area: 'SZ',
  },
  {
    place: 'Lachen SZ',
    area: 'SZ',
  },
  {
    place: 'Siebnen',
    area: 'SZ',
  },
  {
    place: 'Alpthal',
    area: 'SZ',
  },
  {
    place: 'Rigi Kaltbad',
    area: 'SZ',
  },
  {
    place: 'Goldau',
    area: 'SZ',
  },
  {
    place: 'Rigi Klösterli',
    area: 'SZ',
  },
  {
    place: 'Rigi Staffel',
    area: 'SZ',
  },
  {
    place: 'Rigi Kulm',
    area: 'SZ',
  },
  {
    place: 'Oberarth',
    area: 'SZ',
  },
  {
    place: 'Arth',
    area: 'SZ',
  },
  {
    place: 'Illgau',
    area: 'SZ',
  },
  {
    place: 'Brunnen',
    area: 'SZ',
  },
  {
    place: 'Lauerz',
    area: 'SZ',
  },
  {
    place: 'Morschach',
    area: 'SZ',
  },
  {
    place: 'Sisikon',
    area: 'SZ',
  },
  {
    place: 'Stoos SZ',
    area: 'SZ',
  },
  {
    place: 'Muotathal',
    area: 'SZ',
  },
  {
    place: 'Bisisthal',
    area: 'SZ',
  },
  {
    place: 'Ried (Muotathal)',
    area: 'SZ',
  },
  {
    place: 'Klöntal',
    area: 'SZ',
  },
  {
    place: 'Riemenstalden',
    area: 'SZ',
  },
  {
    place: 'Rothenthurm',
    area: 'SZ',
  },
  {
    place: 'Sattel',
    area: 'SZ',
  },
  {
    place: 'Seewen SZ',
    area: 'SZ',
  },
  {
    place: 'Schwyz',
    area: 'SZ',
  },
  {
    place: 'Rickenbach b. Schwyz',
    area: 'SZ',
  },
  {
    place: 'Ibach',
    area: 'SZ',
  },
  {
    place: 'Oberiberg',
    area: 'SZ',
  },
  {
    place: 'Steinen',
    area: 'SZ',
  },
  {
    place: 'Steinerberg',
    area: 'SZ',
  },
  {
    place: 'Unteriberg',
    area: 'SZ',
  },
  {
    place: 'Betschwanden',
    area: 'GL',
  },
  {
    place: 'Bilten',
    area: 'GL',
  },
  {
    place: 'Braunwald',
    area: 'GL',
  },
  {
    place: 'Diesbach',
    area: 'GL',
  },
  {
    place: 'Elm',
    area: 'GL',
  },
  {
    place: 'Engi',
    area: 'GL',
  },
  {
    place: 'Ennenda',
    area: 'GL',
  },
  {
    place: 'Filzbach',
    area: 'GL',
  },
  {
    place: 'Glarus',
    area: 'GL',
  },
  {
    place: 'Haslen',
    area: 'GL',
  },
  {
    place: 'Hätzingen',
    area: 'GL',
  },
  {
    place: 'Kerenzen-Mühlehorn',
    area: 'GL',
  },
  {
    place: 'Leuggelbach',
    area: 'GL',
  },
  {
    place: 'Linthal',
    area: 'GL',
  },
  {
    place: 'Luchsingen',
    area: 'GL',
  },
  {
    place: 'Matt',
    area: 'GL',
  },
  {
    place: 'Mitlödi',
    area: 'GL',
  },
  {
    place: 'Mollis',
    area: 'GL',
  },
  {
    place: 'Mühlehorn',
    area: 'GL',
  },
  {
    place: 'Näfels',
    area: 'GL',
  },
  {
    place: 'Netstal',
    area: 'GL',
  },
  {
    place: 'Nidfurn',
    area: 'GL',
  },
  {
    place: 'Niederurnen',
    area: 'GL',
  },
  {
    place: 'Oberurnen',
    area: 'GL',
  },
  {
    place: 'Obstalden',
    area: 'GL',
  },
  {
    place: 'Riedern',
    area: 'GL',
  },
  {
    place: 'Rüti (GL)',
    area: 'GL',
  },
  {
    place: 'Schwanden (GL)',
    area: 'GL',
  },
  {
    place: 'Schwändi',
    area: 'GL',
  },
  {
    place: 'Sool',
    area: 'GL',
  },
  {
    place: 'Altstätten',
    area: 'SG',
  },
  {
    place: 'Amden',
    area: 'SG',
  },
  {
    place: 'Andwil (SG)',
    area: 'SG',
  },
  {
    place: 'Au (SG)',
    area: 'SG',
  },
  {
    place: 'Bad Ragaz',
    area: 'SG',
  },
  {
    place: 'Balgach',
    area: 'SG',
  },
  {
    place: 'Benken (SG)',
    area: 'SG',
  },
  {
    place: 'Berg (SG)',
    area: 'SG',
  },
  {
    place: 'Berneck',
    area: 'SG',
  },
  {
    place: 'Buchs (SG)',
    area: 'SG',
  },
  {
    place: 'Bütschwil-Ganterschwil',
    area: 'SG',
  },
  {
    place: 'Degersheim',
    area: 'SG',
  },
  {
    place: 'Diepoldsau',
    area: 'SG',
  },
  {
    place: 'Ebnat-Kappel',
    area: 'SG',
  },
  {
    place: 'Eggersriet',
    area: 'SG',
  },
  {
    place: 'Eichberg',
    area: 'SG',
  },
  {
    place: 'Eschenbach (SG)',
    area: 'SG',
  },
  {
    place: 'Flawil',
    area: 'SG',
  },
  {
    place: 'Flums',
    area: 'SG',
  },
  {
    place: 'Gaiserwald',
    area: 'SG',
  },
  {
    place: 'Gams',
    area: 'SG',
  },
  {
    place: 'Goldach',
    area: 'SG',
  },
  {
    place: 'Gommiswald',
    area: 'SG',
  },
  {
    place: 'Gossau (SG)',
    area: 'SG',
  },
  {
    place: 'Grabs',
    area: 'SG',
  },
  {
    place: 'Häggenschwil',
    area: 'SG',
  },
  {
    place: 'Jonschwil',
    area: 'SG',
  },
  {
    place: 'Kaltbrunn',
    area: 'SG',
  },
  {
    place: 'Kirchberg (SG)',
    area: 'SG',
  },
  {
    place: 'Lichtensteig',
    area: 'SG',
  },
  {
    place: 'Lütisburg',
    area: 'SG',
  },
  {
    place: 'Marbach (SG)',
    area: 'SG',
  },
  {
    place: 'Mels',
    area: 'SG',
  },
  {
    place: 'Mörschwil',
    area: 'SG',
  },
  {
    place: 'Mosnang',
    area: 'SG',
  },
  {
    place: 'Muolen',
    area: 'SG',
  },
  {
    place: 'Neckertal',
    area: 'SG',
  },
  {
    place: 'Nesslau',
    area: 'SG',
  },
  {
    place: 'Niederbüren',
    area: 'SG',
  },
  {
    place: 'Niederhelfenschwil',
    area: 'SG',
  },
  {
    place: 'Oberbüren',
    area: 'SG',
  },
  {
    place: 'Oberriet (SG)',
    area: 'SG',
  },
  {
    place: 'Oberuzwil',
    area: 'SG',
  },
  {
    place: 'Pfäfers',
    area: 'SG',
  },
  {
    place: 'Quarten',
    area: 'SG',
  },
  {
    place: 'Rapperswil-Jona',
    area: 'SG',
  },
  {
    place: 'Rebstein',
    area: 'SG',
  },
  {
    place: 'Rheineck',
    area: 'SG',
  },
  {
    place: 'Rorschach',
    area: 'SG',
  },
  {
    place: 'Rorschacherberg',
    area: 'SG',
  },
  {
    place: 'Rüthi',
    area: 'SG',
  },
  {
    place: 'Sargans',
    area: 'SG',
  },
  {
    place: 'Schänis',
    area: 'SG',
  },
  {
    place: 'Schmerikon',
    area: 'SG',
  },
  {
    place: 'Sennwald',
    area: 'SG',
  },
  {
    place: 'Sevelen',
    area: 'SG',
  },
  {
    place: 'St. Gallen',
    area: 'SG',
  },
  {
    place: 'St. Margrethen',
    area: 'SG',
  },
  {
    place: 'Steinach',
    area: 'SG',
  },
  {
    place: 'Thal',
    area: 'SG',
  },
  {
    place: 'Tübach',
    area: 'SG',
  },
  {
    place: 'Untereggen',
    area: 'SG',
  },
  {
    place: 'Uzwil',
    area: 'SG',
  },
  {
    place: 'Vilters-Wangs',
    area: 'SG',
  },
  {
    place: 'Waldkirch',
    area: 'SG',
  },
  {
    place: 'Walenstadt',
    area: 'SG',
  },
  {
    place: 'Wartau',
    area: 'SG',
  },
  {
    place: 'Wattwil',
    area: 'SG',
  },
  {
    place: 'Weesen',
    area: 'SG',
  },
  {
    place: 'Widnau',
    area: 'SG',
  },
  {
    place: 'Wil (SG)',
    area: 'SG',
  },
  {
    place: 'Wildhaus-Alt St. Johann',
    area: 'SG',
  },
  {
    place: 'Wittenbach',
    area: 'SG',
  },
  {
    place: 'Zuzwil (SG)',
    area: 'SG',
  },
  {
    place: 'Aeugst am Albis',
    area: 'ZH',
  },
  {
    place: 'Aeugstertal',
    area: 'ZH',
  },
  {
    place: 'Zwillikon',
    area: 'ZH',
  },
  {
    place: 'Affoltern am Albis',
    area: 'ZH',
  },
  {
    place: 'Bonstetten',
    area: 'ZH',
  },
  {
    place: 'Langnau am Albis',
    area: 'ZH',
  },
  {
    place: 'Hausen am Albis',
    area: 'ZH',
  },
  {
    place: 'Ebertswil',
    area: 'ZH',
  },
  {
    place: 'Hedingen',
    area: 'ZH',
  },
  {
    place: 'Kappel am Albis',
    area: 'ZH',
  },
  {
    place: 'Hauptikon',
    area: 'ZH',
  },
  {
    place: 'Uerzlikon',
    area: 'ZH',
  },
  {
    place: 'Knonau',
    area: 'ZH',
  },
  {
    place: 'Maschwanden',
    area: 'ZH',
  },
  {
    place: 'Mettmenstetten',
    area: 'ZH',
  },
  {
    place: 'Obfelden',
    area: 'ZH',
  },
  {
    place: 'Ottenbach',
    area: 'ZH',
  },
  {
    place: 'Rifferswil',
    area: 'ZH',
  },
  {
    place: 'Adliswil',
    area: 'ZH',
  },
  {
    place: 'Stallikon',
    area: 'ZH',
  },
  {
    place: 'Uetliberg',
    area: 'ZH',
  },
  {
    place: 'Wettswil',
    area: 'ZH',
  },
  {
    place: 'Benken ZH',
    area: 'ZH',
  },
  {
    place: 'Berg am Irchel',
    area: 'ZH',
  },
  {
    place: 'Gräslikon',
    area: 'ZH',
  },
  {
    place: 'Buch am Irchel',
    area: 'ZH',
  },
  {
    place: 'Dachsen',
    area: 'ZH',
  },
  {
    place: 'Dorf',
    area: 'ZH',
  },
  {
    place: 'Feuerthalen',
    area: 'ZH',
  },
  {
    place: 'Langwiesen',
    area: 'ZH',
  },
  {
    place: 'Flaach',
    area: 'ZH',
  },
  {
    place: 'Flurlingen',
    area: 'ZH',
  },
  {
    place: 'Henggart',
    area: 'ZH',
  },
  {
    place: 'Kleinandelfingen',
    area: 'ZH',
  },
  {
    place: 'Alten',
    area: 'ZH',
  },
  {
    place: 'Oerlingen',
    area: 'ZH',
  },
  {
    place: 'Nohl',
    area: 'ZH',
  },
  {
    place: 'Uhwiesen',
    area: 'ZH',
  },
  {
    place: 'Marthalen',
    area: 'ZH',
  },
  {
    place: 'Trüllikon',
    area: 'ZH',
  },
  {
    place: 'Ossingen',
    area: 'ZH',
  },
  {
    place: 'Rheinau',
    area: 'ZH',
  },
  {
    place: 'Ellikon am Rhein',
    area: 'ZH',
  },
  {
    place: 'Thalheim an der Thur',
    area: 'ZH',
  },
  {
    place: 'Rudolfingen',
    area: 'ZH',
  },
  {
    place: 'Wildensbuch',
    area: 'ZH',
  },
  {
    place: 'Truttikon',
    area: 'ZH',
  },
  {
    place: 'Volken',
    area: 'ZH',
  },
  {
    place: 'Bachenbülach',
    area: 'ZH',
  },
  {
    place: 'Bassersdorf',
    area: 'ZH',
  },
  {
    place: 'Bülach',
    area: 'ZH',
  },
  {
    place: 'Dietlikon',
    area: 'ZH',
  },
  {
    place: 'Embrach',
    area: 'ZH',
  },
  {
    place: 'Freienstein',
    area: 'ZH',
  },
  {
    place: 'Teufen ZH',
    area: 'ZH',
  },
  {
    place: 'Glattfelden',
    area: 'ZH',
  },
  {
    place: 'Zweidlen',
    area: 'ZH',
  },
  {
    place: 'Eglisau',
    area: 'ZH',
  },
  {
    place: 'Hochfelden',
    area: 'ZH',
  },
  {
    place: 'Höri',
    area: 'ZH',
  },
  {
    place: 'Hüntwangen',
    area: 'ZH',
  },
  {
    place: 'Kloten',
    area: 'ZH',
  },
  {
    place: 'Nürensdorf',
    area: 'ZH',
  },
  {
    place: 'Lufingen',
    area: 'ZH',
  },
  {
    place: 'Oberembrach',
    area: 'ZH',
  },
  {
    place: 'Kreis 1',
    area: 'ZH',
  },
  {
    place: 'Altstatt',
    area: 'ZH',
  },
  {
    place: 'Kreis 2',
    area: 'ZH',
  },
  {
    place: 'Wollishofen',
    area: 'ZH',
  },
  {
    place: 'Enge',
    area: 'ZH',
  },
  {
    place: 'Laimbach',
    area: 'ZH',
  },
  {
    place: 'Kreis 3',
    area: 'ZH',
  },
  {
    place: 'Wiedikon',
    area: 'ZH',
  },
  {
    place: 'Kreis 4',
    area: 'ZH',
  },
  {
    place: 'Aussersihl',
    area: 'ZH',
  },
  {
    place: 'Kreis 5',
    area: 'ZH',
  },
  {
    place: 'Industriequartier',
    area: 'ZH',
  },
  {
    place: 'Kreis 6',
    area: 'ZH',
  },
  {
    place: 'Kreis 7',
    area: 'ZH',
  },
  {
    place: 'Kreis 8',
    area: 'ZH',
  },
  {
    place: 'Riesbach',
    area: 'ZH',
  },
  {
    place: 'Kreis 9',
    area: 'ZH',
  },
  {
    place: 'Kreis 10',
    area: 'ZH',
  },
  {
    place: 'Kreis 11',
    area: 'ZH',
  },
  {
    place: 'Kreis 12',
    area: 'ZH',
  },
  {
    place: 'Schamendingen',
    area: 'ZH',
  },
  {
    place: 'Opfikon',
    area: 'ZH',
  },
  {
    place: 'Glattpark (Opfikon)',
    area: 'ZH',
  },
  {
    place: 'Rafz',
    area: 'ZH',
  },
  {
    place: 'Rorbas',
    area: 'ZH',
  },
  {
    place: 'Wallisellen',
    area: 'ZH',
  },
  {
    place: 'Wasterkingen',
    area: 'ZH',
  },
  {
    place: 'Wil ZH',
    area: 'ZH',
  },
  {
    place: 'Winkel',
    area: 'ZH',
  },
  {
    place: 'Boppelsen',
    area: 'ZH',
  },
  {
    place: 'Regensberg',
    area: 'ZH',
  },
  {
    place: 'Dällikon',
    area: 'ZH',
  },
  {
    place: 'Dänikon ZH',
    area: 'ZH',
  },
  {
    place: 'Dielsdorf',
    area: 'ZH',
  },
  {
    place: 'Hüttikon',
    area: 'ZH',
  },
  {
    place: 'Steinmaur',
    area: 'ZH',
  },
  {
    place: 'Bachs',
    area: 'ZH',
  },
  {
    place: 'Neerach',
    area: 'ZH',
  },
  {
    place: 'Niederglatt ZH',
    area: 'ZH',
  },
  {
    place: 'Niederhasli',
    area: 'ZH',
  },
  {
    place: 'Nassenwil',
    area: 'ZH',
  },
  {
    place: 'Oberhasli',
    area: 'ZH',
  },
  {
    place: 'Niederweningen',
    area: 'ZH',
  },
  {
    place: 'Oberglatt ZH',
    area: 'ZH',
  },
  {
    place: 'Siglistorf',
    area: 'ZH',
  },
  {
    place: 'Oberweningen',
    area: 'ZH',
  },
  {
    place: 'Buchs ZH',
    area: 'ZH',
  },
  {
    place: 'Otelfingen',
    area: 'ZH',
  },
  {
    place: 'Regensdorf',
    area: 'ZH',
  },
  {
    place: 'Watt',
    area: 'ZH',
  },
  {
    place: 'Glattbrugg',
    area: 'ZH',
  },
  {
    place: 'Rümlang',
    area: 'ZH',
  },
  {
    place: 'Schleinikon',
    area: 'ZH',
  },
  {
    place: 'Schöfflisdorf',
    area: 'ZH',
  },
  {
    place: 'Windlach',
    area: 'ZH',
  },
  {
    place: 'Sünikon',
    area: 'ZH',
  },
  {
    place: 'Weiach',
    area: 'ZH',
  },
  {
    place: 'Bäretswil',
    area: 'ZH',
  },
  {
    place: 'Bubikon',
    area: 'ZH',
  },
  {
    place: 'Wolfhausen',
    area: 'ZH',
  },
  {
    place: 'Tann',
    area: 'ZH',
  },
  {
    place: 'Dürnten',
    area: 'ZH',
  },
  {
    place: 'Steg im Tösstal',
    area: 'ZH',
  },
  {
    place: 'Fischenthal',
    area: 'ZH',
  },
  {
    place: 'Gibswil',
    area: 'ZH',
  },
  {
    place: 'Bertschikon (Gossau ZH)',
    area: 'ZH',
  },
  {
    place: 'Grüt (Gossau ZH)',
    area: 'ZH',
  },
  {
    place: 'Gossau ZH',
    area: 'ZH',
  },
  {
    place: 'Ottikon (Gossau ZH)',
    area: 'ZH',
  },
  {
    place: 'Grüningen',
    area: 'ZH',
  },
  {
    place: 'Hinwil',
    area: 'ZH',
  },
  {
    place: 'Wernetshausen',
    area: 'ZH',
  },
  {
    place: 'Rüti ZH',
    area: 'ZH',
  },
  {
    place: 'Wald ZH',
    area: 'ZH',
  },
  {
    place: 'Aathal-Seegräben',
    area: 'ZH',
  },
  {
    place: 'Laupen ZH',
    area: 'ZH',
  },
  {
    place: 'Adetswil',
    area: 'ZH',
  },
  {
    place: 'Wetzikon ZH',
    area: 'ZH',
  },
  {
    place: 'Kilchberg ZH',
    area: 'ZH',
  },
  {
    place: 'Oberrieden',
    area: 'ZH',
  },
  {
    place: 'Richterswil',
    area: 'ZH',
  },
  {
    place: 'Samstagern',
    area: 'ZH',
  },
  {
    place: 'Rüschlikon',
    area: 'ZH',
  },
  {
    place: 'Gattikon',
    area: 'ZH',
  },
  {
    place: 'Thalwil',
    area: 'ZH',
  },
  {
    place: 'Erlenbach ZH',
    area: 'ZH',
  },
  {
    place: 'Herrliberg',
    area: 'ZH',
  },
  {
    place: 'Feldbach',
    area: 'ZH',
  },
  {
    place: 'Küsnacht ZH',
    area: 'ZH',
  },
  {
    place: 'Oetwil am See',
    area: 'ZH',
  },
  {
    place: 'Männedorf',
    area: 'ZH',
  },
  {
    place: 'Meilen',
    area: 'ZH',
  },
  {
    place: 'Hombrechtikon',
    area: 'ZH',
  },
  {
    place: 'Stäfa',
    area: 'ZH',
  },
  {
    place: 'Uerikon',
    area: 'ZH',
  },
  {
    place: 'Uetikon am See',
    area: 'ZH',
  },
  {
    place: 'Zumikon',
    area: 'ZH',
  },
  {
    place: 'Zollikerberg',
    area: 'ZH',
  },
  {
    place: 'Zollikon',
    area: 'ZH',
  },
  {
    place: 'Illnau',
    area: 'ZH',
  },
  {
    place: 'Fehraltorf',
    area: 'ZH',
  },
  {
    place: 'Hittnau',
    area: 'ZH',
  },
  {
    place: 'Kemptthal',
    area: 'ZH',
  },
  {
    place: 'Grafstal',
    area: 'ZH',
  },
  {
    place: 'Winterberg ZH',
    area: 'ZH',
  },
  {
    place: 'Lindau',
    area: 'ZH',
  },
  {
    place: 'Tagelswangen',
    area: 'ZH',
  },
  {
    place: 'Pfäffikon ZH',
    area: 'ZH',
  },
  {
    place: 'Auslikon',
    area: 'ZH',
  },
  {
    place: 'Madetswil',
    area: 'ZH',
  },
  {
    place: 'Gündisau',
    area: 'ZH',
  },
  {
    place: 'Russikon',
    area: 'ZH',
  },
  {
    place: 'Rumlikon',
    area: 'ZH',
  },
  {
    place: 'Agasul',
    area: 'ZH',
  },
  {
    place: 'Weisslingen',
    area: 'ZH',
  },
  {
    place: 'Neschwil',
    area: 'ZH',
  },
  {
    place: 'Theilingen',
    area: 'ZH',
  },
  {
    place: 'Wila',
    area: 'ZH',
  },
  {
    place: 'Saland',
    area: 'ZH',
  },
  {
    place: 'Turbenthal',
    area: 'ZH',
  },
  {
    place: 'Wildberg',
    area: 'ZH',
  },
  {
    place: 'Schalchen',
    area: 'ZH',
  },
  {
    place: 'Ehrikon',
    area: 'ZH',
  },
  {
    place: 'Gockhausen',
    area: 'ZH',
  },
  {
    place: 'Dübendorf',
    area: 'ZH',
  },
  {
    place: 'Schwerzenbach',
    area: 'ZH',
  },
  {
    place: 'Egg b. Zürich',
    area: 'ZH',
  },
  {
    place: 'Hinteregg',
    area: 'ZH',
  },
  {
    place: 'Esslingen',
    area: 'ZH',
  },
  {
    place: 'Fällanden',
    area: 'ZH',
  },
  {
    place: 'Pfaffhausen',
    area: 'ZH',
  },
  {
    place: 'Benglen',
    area: 'ZH',
  },
  {
    place: 'Greifensee',
    area: 'ZH',
  },
  {
    place: 'Binz',
    area: 'ZH',
  },
  {
    place: 'Ebmatingen',
    area: 'ZH',
  },
  {
    place: 'Maur',
    area: 'ZH',
  },
  {
    place: 'Forch',
    area: 'ZH',
  },
  {
    place: 'Gutenswil',
    area: 'ZH',
  },
  {
    place: 'Nänikon',
    area: 'ZH',
  },
  {
    place: 'Uster',
    area: 'ZH',
  },
  {
    place: 'Sulzbach',
    area: 'ZH',
  },
  {
    place: 'Wermatswil',
    area: 'ZH',
  },
  {
    place: 'Freudwil',
    area: 'ZH',
  },
  {
    place: 'Riedikon',
    area: 'ZH',
  },
  {
    place: 'Mönchaltorf',
    area: 'ZH',
  },
  {
    place: 'Volketswil',
    area: 'ZH',
  },
  {
    place: 'Brüttisellen',
    area: 'ZH',
  },
  {
    place: 'Wangen b. Dübendorf',
    area: 'ZH',
  },
  {
    place: 'Altikon',
    area: 'ZH',
  },
  {
    place: 'Brütten',
    area: 'ZH',
  },
  {
    place: 'Rutschwil (Dägerlen)',
    area: 'ZH',
  },
  {
    place: 'Dägerlen',
    area: 'ZH',
  },
  {
    place: 'Oberwil (Dägerlen)',
    area: 'ZH',
  },
  {
    place: 'Berg (Dägerlen)',
    area: 'ZH',
  },
  {
    place: 'Dättlikon',
    area: 'ZH',
  },
  {
    place: 'Dinhard',
    area: 'ZH',
  },
  {
    place: 'Ellikon an der Thur',
    area: 'ZH',
  },
  {
    place: 'Elsau',
    area: 'ZH',
  },
  {
    place: 'Gerlikon',
    area: 'ZH',
  },
  {
    place: 'Hagenbuch ZH',
    area: 'ZH',
  },
  {
    place: 'Hettlingen',
    area: 'ZH',
  },
  {
    place: 'Aesch (Neftenbach)',
    area: 'ZH',
  },
  {
    place: 'Riet (Neftenbach)',
    area: 'ZH',
  },
  {
    place: 'Hünikon (Neftenbach)',
    area: 'ZH',
  },
  {
    place: 'Neftenbach',
    area: 'ZH',
  },
  {
    place: 'Pfungen',
    area: 'ZH',
  },
  {
    place: 'Rickenbach ZH',
    area: 'ZH',
  },
  {
    place: 'Rickenbach Sulz',
    area: 'ZH',
  },
  {
    place: 'Hofstetten ZH',
    area: 'ZH',
  },
  {
    place: 'Schlatt ZH',
    area: 'ZH',
  },
  {
    place: 'Seuzach',
    area: 'ZH',
  },
  {
    place: 'Bichelsee',
    area: 'ZH',
  },
  {
    place: 'Schmidrüti',
    area: 'ZH',
  },
  {
    place: 'Ricketwil (Winterthur)',
    area: 'ZH',
  },
  {
    place: 'Reutlingen (Winterthur)',
    area: 'ZH',
  },
  {
    place: 'Stadel (Winterthur)',
    area: 'ZH',
  },
  {
    place: 'Winterthur',
    area: 'ZH',
  },
  {
    place: 'Sennhof (Winterthur)',
    area: 'ZH',
  },
  {
    place: 'Wiesendangen',
    area: 'ZH',
  },
  {
    place: 'Kollbrunn',
    area: 'ZH',
  },
  {
    place: 'Rikon im Tösstal',
    area: 'ZH',
  },
  {
    place: 'Zell ZH',
    area: 'ZH',
  },
  {
    place: 'Rämismühle',
    area: 'ZH',
  },
  {
    place: 'Aesch ZH',
    area: 'ZH',
  },
  {
    place: 'Uitikon Waldegg',
    area: 'ZH',
  },
  {
    place: 'Birmensdorf ZH',
    area: 'ZH',
  },
  {
    place: 'Geroldswil',
    area: 'ZH',
  },
  {
    place: 'Oberengstringen',
    area: 'ZH',
  },
  {
    place: 'Oetwil an der Limmat',
    area: 'ZH',
  },
  {
    place: 'Urdorf',
    area: 'ZH',
  },
  {
    place: 'Unterengstringen',
    area: 'ZH',
  },
  {
    place: 'Weiningen ZH',
    area: 'ZH',
  },
  {
    place: 'Fahrweid',
    area: 'ZH',
  },
  {
    place: 'Zürich',
    area: 'ZH',
  },
  {
    place: 'Schlieren',
    area: 'ZH',
  },
  {
    place: 'Andelfingen',
    area: 'ZH',
  },
  {
    place: 'Adlikon b. Andelfingen',
    area: 'ZH',
  },
  {
    place: 'Humlikon',
    area: 'ZH',
  },
  {
    place: 'Waltalingen',
    area: 'ZH',
  },
  {
    place: 'Guntalingen',
    area: 'ZH',
  },
  {
    place: 'Unterstammheim',
    area: 'ZH',
  },
  {
    place: 'Oberstammheim',
    area: 'ZH',
  },
  {
    place: 'Wilen b. Neunforn',
    area: 'ZH',
  },
  {
    place: 'Au ZH',
    area: 'ZH',
  },
  {
    place: 'Wädenswil',
    area: 'ZH',
  },
  {
    place: 'Schönenberg ZH',
    area: 'ZH',
  },
  {
    place: 'Hütten',
    area: 'ZH',
  },
  {
    place: 'Elgg',
    area: 'ZH',
  },
  {
    place: 'Dickbuch',
    area: 'ZH',
  },
  {
    place: 'Aadorf',
    area: 'ZH',
  },
  {
    place: 'Sihlbrugg',
    area: 'ZH',
  },
  {
    place: 'Sihlbrugg Station',
    area: 'ZH',
  },
  {
    place: 'Sihlwald',
    area: 'ZH',
  },
  {
    place: 'Horgen',
    area: 'ZH',
  },
  {
    place: 'Horgenberg',
    area: 'ZH',
  },
  {
    place: 'Hirzel',
    area: 'ZH',
  },
  {
    place: 'Effretikon',
    area: 'ZH',
  },
  {
    place: 'Kyburg',
    area: 'ZH',
  },
  {
    place: 'Bauma',
    area: 'ZH',
  },
  {
    place: 'Sternenberg',
    area: 'ZH',
  },
  {
    place: 'Bertschikon',
    area: 'ZH',
  },
  {
    place: 'Gundetswil',
    area: 'ZH',
  },
  {
    place: 'Kefikon ZH',
    area: 'ZH',
  },
  {
    place: 'Attikon',
    area: 'ZH',
  },
  {
    place: 'Menzengrüt',
    area: 'ZH',
  },
];

class AppsDataGenerator {
  constructor(buildFolder: string) {
    console.log('buildFolder:', buildFolder);
    this.generateSiteMap(buildFolder);
  }

  private generateSiteMap(outDir: string) {
    const domain = `https://lokale.events/`;
    const lastMod = new Date().toISOString();
    const links = this.generateUpcomingSitemapLinks(lastMod);
    const smStream = new SitemapStream({
      hostname: domain,
      lastmodDateOnly: false,
      xmlns: {
        news: false,
        xhtml: true,
        image: false,
        video: false,
      },
    });
    streamToPromise(Readable.from(links).pipe(smStream)).then((sitemap) =>
      this.writeFile(join(outDir, `sitemap.xml`), String(sitemap)),
    );
  }

  private generateUpcomingSitemapLinks(lastMod: string): Array<{
    url: string;
    changefreq: 'daily' | 'weekly' | 'monthly' | 'yearly';
    lastmod: string;
    priority: number;
  }> {
    const links = [
      {
        url: '/',
        changefreq: 'daily' as const,
        lastmod: lastMod,
        priority: 1.0,
      },
      {
        url: '/ueber-uns/',
        changefreq: 'monthly' as const,
        lastmod: lastMod,
        priority: 0.8,
      },
      {
        url: '/agb/',
        changefreq: 'yearly' as const,
        lastmod: lastMod,
        priority: 0.3,
      },
    ]; // Add location-based event pages with better priorities
    const locationUrls = places.map((location) => {
      return `CH/${location.area.toUpperCase()}/${encodeURIComponent(location.place)}`;
    });
    locationUrls.forEach((location) => {
      ['heute', 'morgen', 'kommendes-wochenende'].forEach((day) => {
        const dateUrl = `/events/in/${location}/${day}`;
        links.push({
          url: dateUrl,
          changefreq: 'daily' as const,
          lastmod: lastMod,
          priority: day != 'kommendes-wochenende' ? 0.9 : 0.7, // Higher priority for today
        });
      });
    });
    return links;
  }

  private writeFile(file: string, data: string) {
    console.log(`* ${file}`);
    writeFileSync(file, data);
  }
}

new AppsDataGenerator(join(process.cwd(), 'apps/upcoming/public'));
