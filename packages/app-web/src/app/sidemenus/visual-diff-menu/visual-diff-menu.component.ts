import { Component, OnInit } from '@angular/core';
import { SourceSubscriptionService } from '../../services/source-subscription.service';
import { AuthService } from '../../services/auth.service';
import { SourceSubscription } from '../../graphql/types';
import { ProductConfig, ProductService } from '../../services/product.service';

@Component({
  selector: 'app-visual-diff-menu',
  templateUrl: './visual-diff-menu.component.html',
  styleUrls: ['./visual-diff-menu.component.scss'],
})
export class VisualDiffMenuComponent implements OnInit {

  subscriptions: SourceSubscription[] = []

  constructor(private readonly sourceSubscriptionService: SourceSubscriptionService,
              private readonly authService: AuthService) {}

  ngOnInit(): void {
    this.authService.authorizationChange().subscribe(authenticated => {
      if (authenticated) {
        this.fetchSubscriptions()
      }
    })
  }

  private async fetchSubscriptions() {
    const page = 0;
    const sources = await this.sourceSubscriptionService.listSourceSubscriptions({
      cursor: {
        page,
      },
    });
    this.subscriptions.push(...sources);
  }

  getPageUrl(sub: SourceSubscription): string {
    const url = sub.sources[0].page.url;
    return new URL(url).hostname;
  }
}
