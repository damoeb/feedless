import { SitemapStream, streamToPromise } from 'sitemap';
import { writeFileSync } from 'fs';
import { join } from 'path';
import { Readable } from 'node:stream';

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
    ];

    // Add location-based event pages with better priorities
    const locationUrls = [
      'CH/ZH/Affoltern%2520am%2520Albis/',
      'CH/ZH/Birmensdorf/',
      'CH/ZH/R%25C3%25BCschlikon/',
      'CH/ZH/Thalwil/',
      'CH/AG/Wohlen%2520AG/',
      'CH/ZH/Urdorf/',
      'CH/ZH/Hedingen/',
      'CH/SG/Uznach/',
      'CH/SG/Untereggen/',
      'CH/SG/Wattwil/',
      'CH/SG/Weesen/',
      'CH/SG/Wartau/',
      'CH/SG/Wildnau/',
      'CH/SG/Vilters-Wangs/',
      'CH/SG/Wittenbach/',
      'CH/SG/Walenstadt/',
      'CH/SG/Zuzwil/',
      'CH/SG/Thal/',
      'CH/SG/St.%20Margarethen/',
      'CH/SG/Rorschacherberg/',
      'CH/SG/Schänis/',
      'CH/SG/Sargans/',
      'CH/SG/Sennwald/',
      'CH/SG/Rorschach/',
      'CH/SG/Sevelen/',
      'CH/SG/Rüthi/',
      'CH/SG/Steinach/',
      'CH/SG/Niederbüren/',
      'CH/SG/Mörschwil/',
      'CH/SG/Oberriet/',
      'CH/SG/Rheineck/',
      'CH/SG/Rebstein/',
      'CH/SG/Muolen/',
      'CH/SG/Oberuzwil/',
      'CH/SG/Nesslau/',
      'CH/SG/Oberbüren/',
      'CH/SG/Neckertal/',
      'CH/SG/Mels/',
      'CH/SG/Marbach/',
      'CH/SG/Gommiswald/',
      'CH/SG/Gossau/',
      'CH/SG/Lichtensteig/',
      'CH/SG/Grabs/',
      'CH/SG/Kirchberg/',
      'CH/SG/Jonschwil/',
      'CH/SG/Lütisburg/',
      'CH/SG/Häggenschwil/',
      'CH/SG/Eichberg/',
      'CH/SG/Gams/',
      'CH/SG/Eschenbach/',
      'CH/SG/Ebnat Kappel/',
      'CH/SG/Diepodsau/',
      'CH/SG/Gaiserwald/',
      'CH/SG/Flums/',
      'CH/SG/Eggersriet/',
      'CH/SG/Goldach/',
      'CH/SG/Flawil/',
      'CH/SG/Au/',
      'CH/SG/Bad%20Ragaz/',
      'CH/SG/Altstätten/',
      'CH/SG/Buchs/',
      'CH/SG/Berneck/',
      'CH/SG/Degersheim/',
      'CH/SG/Berg/',
      'CH/SG/Bütschwil-Ganterschwil/',
      'CH/ZH/Stammheim/',
      'CH/ZH/Oetwil/',
      'CH/ZH/Elgg/',
      'CH/ZH/Horgen/',
      'CH/ZH/Weiningen/',
      'CH/ZH/Andelfingen/',
      'CH/ZH/Unterengstringen/',
      'CH/ZH/Wädenswil/',
      'CH/ZH/Bauma/',
      'CH/ZH/Urdorf/',
      'CH/ZH/Geroldswil/',
      'CH/ZH/Oberengstringen/',
      'CH/ZH/Pfungen/',
      'CH/ZH/Birmensdorf/',
      'CH/ZH/Zell/',
      'CH/ZH/Wiesendangen/',
      'CH/ZH/Aesch/',
      'CH/ZH/Neftenbach/',
      'CH/ZH/Rickenbach/',
      'CH/ZH/Seuzach/',
      'CH/ZH/Ellikon%20an%20der%20Thur/',
      'CH/ZH/Hagenbuch/',
      'CH/ZH/Dättlikon/',
      'CH/ZH/Wildberg/',
      'CH/ZH/Altikon/',
      'CH/ZH/Elsau/',
      'CH/ZH/Hettlingen/',
      'CH/ZH/Dägerlen/',
      'CH/ZH/Dinhard/',
      'CH/ZH/Turbenthal/',
      'CH/ZH/Fischenthal/',
      'CH/ZH/Hittnau/',
      'CH/ZH/Illnau%20Effretikon/',
      'CH/ZH/Russikon/',
      'CH/ZH/Fehraltorf/',
      'CH/ZH/Herrliberg/',
      'CH/ZH/Weisslingen/',
      'CH/ZH/Lindau/',
      'CH/ZH/Dürnten/',
      'CH/ZH/Wila/',
      'CH/ZH/Buchs/',
      'CH/ZH/Schleinikon/',
      'CH/ZH/Rümlang/',
      'CH/ZH/Oberweningen/',
      'CH/ZH/Bäretswil/',
      'CH/ZH/Bubikon/',
      'CH/ZH/Schöfflisdorf/',
      'CH/ZH/Regensdorf/',
      'CH/ZH/Otelfingen/',
      'CH/ZH/Weiach/',
      'CH/ZH/Regensberg/',
      'CH/ZH/Hüttikon/',
      'CH/ZH/Steinmaur/',
      'CH/ZH/Daellikon/',
      'CH/ZH/Niederhasli/',
      'CH/ZH/Dielsdorf/',
      'CH/ZH/Oberglatt/',
      'CH/ZH/Niederweningen/',
      'CH/ZH/Neerach/',
      'CH/ZH/Daenikon/',
      'CH/ZH/Wasterkingen/',
      'CH/ZH/Rorbas/',
      'CH/ZH/Opfikon/',
      'CH/ZH/Lufingen/',
      'CH/ZH/Rafz/',
      'CH/ZH/Wil/',
      'CH/ZH/Winkel/',
      'CH/ZH/Oberembrach/',
      'CH/ZH/Boppelsen/',
      'CH/ZH/Wallisellen/',
      'CH/ZH/Hüntwangen/',
      'CH/ZH/Freienstein-Teufen/',
      'CH/ZH/Glattfelden/',
      'CH/ZH/Hoeri/',
      'CH/ZH/Bülach/',
      'CH/ZH/Bachenbülach/',
      'CH/ZH/Bassersdorf/',
      'CH/ZH/Embrach/',
      'CH/ZH/Kloten/',
      'CH/ZH/Dietlikon/',
      'CH/ZH/Volken/',
      'CH/ZH/Kleinandelfingen/',
      'CH/ZH/Truellikon/',
      'CH/ZH/Flaach/',
      'CH/ZH/Flurlingen/',
      'CH/ZH/Marthalen/',
      'CH/ZH/Rheinau/',
      'CH/ZH/Henggart/',
      'CH/ZH/Ossingen/',
      'CH/ZH/Uhwiesen/',
      'CH/ZH/Dorf/',
      'CH/ZH/Dachsen/',
      'CH/ZH/Wettswil%20am%20Albis/',
      'CH/ZH/Feuerthalen/',
      'CH/ZH/Benken/',
    ];

    locationUrls.forEach((location) => {
      ['heute', 'morgen', 'kommendes-wochenende'].forEach((day) => {
        const dateUrl = `/events/in/${location}${day}`;
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
