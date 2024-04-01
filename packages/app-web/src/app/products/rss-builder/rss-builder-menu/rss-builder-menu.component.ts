import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
} from '@angular/core';
import { SourceSubscriptionService } from '../../../services/source-subscription.service';
import { AuthService } from '../../../services/auth.service';
import { SourceSubscription } from '../../../graphql/types';
import { filter } from 'rxjs';

@Component({
  selector: 'app-rss-builder-menu',
  templateUrl: './rss-builder-menu.component.html',
  styleUrls: ['./rss-builder-menu.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RssBuilderMenuComponent implements OnInit {
  feeds: SourceSubscription[] = [];

  constructor(
    private readonly sourceSubscriptionService: SourceSubscriptionService,
    private readonly authService: AuthService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.authService
      .authorizationChange()
      .pipe(filter((authenticated) => authenticated?.loggedIn))
      .subscribe((authenticated) => {
        if (authenticated.loggedIn) {
          this.fetchFeeds();
        }
      });
  }

  getPageUrl(sub: SourceSubscription): string {
    const url = sub.sources[0].page.url;
    return new URL(url).hostname;
  }

  private async fetchFeeds() {
    const page = 0;
    this.feeds = await this.sourceSubscriptionService.listSourceSubscriptions({
      cursor: {
        page,
      },
    });
    this.changeRef.detectChanges();
  }
}
