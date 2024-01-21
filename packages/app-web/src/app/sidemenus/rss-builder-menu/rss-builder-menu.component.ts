import { Component, OnInit } from '@angular/core';
import { SourceSubscriptionService } from '../../services/source-subscription.service';
import { AuthService } from '../../services/auth.service';
import { SourceSubscription } from '../../graphql/types';

@Component({
  selector: 'app-rss-builder-menu',
  templateUrl: './rss-builder-menu.component.html',
  styleUrls: ['./rss-builder-menu.component.scss'],
})
export class RssBuilderMenuComponent implements OnInit {

  feeds: SourceSubscription[] = []

  constructor(private readonly sourceSubscriptionService: SourceSubscriptionService,
              private readonly authService: AuthService) {}

  ngOnInit(): void {
    this.authService.authorizationChange().subscribe(authenticated => {
      if (authenticated) {
        this.fetchFeeds()
      }
    })
  }

  private async fetchFeeds() {
    const page = 0;
    const sources = await this.sourceSubscriptionService.listSourceSubscriptions({
      cursor: {
        page,
      },
    });
    this.feeds.push(...sources);
  }

  getPageUrl(sub: SourceSubscription): string {
    const url = sub.sources[0].page.url;
    return new URL(url).hostname;
  }
}
