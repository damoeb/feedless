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
  selector: 'app-checkout-cancel-page',
  templateUrl: './checkout-cancel.page.html',
  styleUrls: ['./checkout-cancel.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonContent, IonButton, RouterLink],
  standalone: true,
})
export class CheckoutCancelPage implements OnInit {
  private readonly pageService = inject(PageService);

  ngOnInit() {
    this.pageService.setMetaTags({
      title: 'Checkout abgebrochen – Edikte Alerts',
      description: 'Stripe Checkout abgebrochen.',
      publisher: 'feedless',
      category: 'Alerts',
      url: 'https://edikte.feedless.org/checkout/abbruch',
      lang: 'de',
      publishedAt: dayjs(),
      keywords: ['Checkout'],
      author: 'feedless Team',
      robots: 'noindex, nofollow',
      canonicalUrl: 'https://edikte.feedless.org/checkout/abbruch',
    } satisfies PageTags);
  }
}
