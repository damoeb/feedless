import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { IonButton, IonContent } from '@ionic/angular/standalone';
import { PageService, PageTags } from '@feedless/components';
import dayjs from 'dayjs';

@Component({
  selector: 'app-checkout-success-page',
  templateUrl: './checkout-success.page.html',
  styleUrls: ['./checkout-success.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonContent, IonButton, RouterLink],
  standalone: true,
})
export class CheckoutSuccessPage implements OnInit {
  private readonly pageService = inject(PageService);

  ngOnInit() {
    this.pageService.setMetaTags({
      title: 'Checkout – Edikte Alerts',
      description: 'Stripe Checkout abgeschlossen.',
      publisher: 'feedless',
      category: 'Alerts',
      url: 'https://edikte.feedless.org/checkout/erfolg',
      lang: 'de',
      publishedAt: dayjs(),
      keywords: ['Checkout'],
      author: 'feedless Team',
      robots: 'noindex, nofollow',
      canonicalUrl: 'https://edikte.feedless.org/checkout/erfolg',
    } satisfies PageTags);
  }
}
