import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
} from '@angular/core';
import { IonContent, IonHeader } from '@ionic/angular/standalone';
import { RouterLink } from '@angular/router';
import { PageService, PageTags } from '@feedless/components';
import dayjs from 'dayjs';

@Component({
  selector: 'app-upcoming-about-us-page',
  templateUrl: './about-us.page.html',
  styleUrls: ['./about-us.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonHeader, IonContent, RouterLink],
  standalone: true,
})
export class AboutUsPage implements OnInit {
  private readonly pageService = inject(PageService);

  ngOnInit() {
    this.pageService.setMetaTags(this.getPageTags());
  }

  private getPageTags(): PageTags {
    return {
      title:
        'Über lokale.events | Entdecke lokale Veranstaltungen in deiner Region',
      description:
        'Erfahre mehr über lokale.events - die Plattform für lokale Veranstaltungen und Events. Wir bringen Menschen zusammen und machen regionale Schätze sichtbar.',
      publisher: 'lokale.events',
      category: 'Über uns',
      url: document.location.href,
      lang: 'de',
      publishedAt: dayjs(),
      keywords: [
        'lokale.events',
        'über uns',
        'lokale Veranstaltungen',
        'Events',
        'Gemeinschaft',
        'regional',
      ],
      author: 'lokale.events Team',
      robots: 'index, follow',
      canonicalUrl: 'https://lokale.events/ueber-uns',
    };
  }
}
