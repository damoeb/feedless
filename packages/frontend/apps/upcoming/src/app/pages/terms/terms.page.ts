import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
} from '@angular/core';
import { IonContent, IonHeader } from '@ionic/angular/standalone';
import { PageService, PageTags } from '@feedless/services';
import dayjs from 'dayjs';

@Component({
  selector: 'app-upcoming-terms-page',
  templateUrl: './terms.page.html',
  styleUrls: ['./terms.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonHeader, IonContent],
  standalone: true,
})
export class TermsPage implements OnInit {
  private readonly pageService = inject(PageService);

  ngOnInit() {
    this.pageService.setMetaTags(this.getPageTags());
  }

  private getPageTags(): PageTags {
    return {
      title: 'Allgemeine Geschäftsbedingungen | lokale.events',
      description:
        'Allgemeine Geschäftsbedingungen und Datenschutzerklärung von lokale.events. Informationen zum Datenschutz und zur Nutzung der Plattform.',
      publisher: 'lokale.events',
      category: 'Rechtliches',
      url: document.location.href,
      lang: 'de',
      publishedAt: dayjs(),
      keywords: [
        'AGB',
        'Allgemeine Geschäftsbedingungen',
        'Datenschutz',
        'lokale.events',
        'Nutzungsbedingungen',
      ],
      author: 'lokale.events Team',
      robots: 'index, follow',
      canonicalUrl: 'https://lokale.events/agb',
    };
  }
}
