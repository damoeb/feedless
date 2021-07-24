import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnInit,
} from '@angular/core';
import { ModalController } from '@ionic/angular';
import { Apollo, gql } from 'apollo-angular';

import { GqlArticle, GqlDiscoveredFeed } from '../../../generated/graphql';

@Component({
  selector: 'app-feed',
  templateUrl: './feed.component.html',
  styleUrls: ['./feed.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeedComponent implements OnInit {
  @Input()
  feed: GqlDiscoveredFeed;
  @Input()
  canSubscribe = false;
  articles: GqlArticle[] = [];

  constructor(
    private readonly modalController: ModalController,
    private readonly changeDetectorRef: ChangeDetectorRef,
    private readonly apollo: Apollo
  ) {}

  ngOnInit() {
    this.getArticles().subscribe((response: any) => {
      this.articles = response.data.articlesForFeedUrl;
      this.changeDetectorRef.detectChanges();
    });
  }

  getArticles() {
    return this.apollo.query<any>({
      query: gql`
        query {
          articlesForFeedUrl(feedUrl: "${this.feed.url}") {
            id
            date_published
            url
            author
            title
            content_text
            tags
          }
        }
      `,
    });
  }

  dismissModal() {
    return this.modalController.dismiss();
  }

  subscribe() {
    return this.modalController.dismiss(this.feed);
  }
}
