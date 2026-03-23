import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
} from '@angular/core';
import { IonContent, ToastController } from '@ionic/angular/standalone';

import { PageService, PageTags } from '@feedless/components';

import dayjs from 'dayjs';
import { ArticleViewComponent } from '../../components/article-view/article-view.component';
import { FeedItemsListComponent } from '../../components/feed-items-list/feed-items-list.component';
import { FeedsSidebarComponent } from '../../components/feeds-sidebar/feeds-sidebar.component';
import { Feed, FeedItem } from '../../data/feeds';

@Component({
  selector: 'app-feed-subscriptions-page',
  templateUrl: './feed-subscriptions.page.html',
  styleUrls: ['./feed-subscriptions.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IonContent,
    FeedsSidebarComponent,
    FeedItemsListComponent,
    ArticleViewComponent,
  ],
  standalone: true,
})
export class FeedSubscriptionsPage implements OnInit {
  private readonly pageService = inject(PageService);
  private readonly toastCtrl = inject(ToastController);

  initialFeeds: Feed[] = [];
  items: FeedItem[] = [];
  selectedFeedId: string | null = null;
  selectedView = 'all';
  selectedItemId: string | null = null;
  selectedItem: FeedItem | null = null;
  viewTitle = '';

  handleSidebarSelectFeed(feedId: string): void {
    this.selectedFeedId = feedId || null;
  }

  handleSidebarSelectView(view: string): void {
    this.selectedView = view;
  }

  handleSelectItem(id: string): void {
    this.selectedItemId = id;
    this.selectedItem = this.items.find((i) => i.id === id) ?? null;
  }

  handleToggleStar(id: string): void {
    this.items = this.items.map((it) =>
      it.id === id ? { ...it, starred: !it.starred } : it,
    );
    if (this.selectedItem?.id === id) {
      const next = this.items.find((i) => i.id === id);
      this.selectedItem = next ?? null;
    }
  }

  ngOnInit() {
    this.pageService.setMetaTags(this.getPageTags());
  }

  private getPageTags(): PageTags {
    return {
      title: 'Edikte Alerts',
      description:
        'Erfahre mehr über lokale.events - die Plattform für lokale Veranstaltungen und Events. Wir bringen Menschen zusammen und machen regionale Schätze sichtbar.',
      publisher: 'feedless',
      category: 'Alerts',
      url: 'https://edikte.feedless.org',
      lang: 'de',
      publishedAt: dayjs(),
      keywords: ['Versteigerungen', 'Email', 'Alerts', 'Notifications'],
      author: 'feedless Team',
      robots: 'index, follow',
      canonicalUrl: 'https://edikte.feedless.org',
    };
  }
}
