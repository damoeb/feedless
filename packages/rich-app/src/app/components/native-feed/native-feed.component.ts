import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnInit,
} from '@angular/core';
import { ModalController } from '@ionic/angular';
import { Apollo, gql } from 'apollo-angular';

import {
  GqlArticle,
  GqlArticleRef,
  GqlNativeFeedRef,
} from '../../../generated/graphql';

@Component({
  selector: 'app-native-feed',
  templateUrl: './native-feed.component.html',
  styleUrls: ['./native-feed.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NativeFeedComponent implements OnInit {
  @Input()
  feed: GqlNativeFeedRef;
  @Input()
  canSubscribe = false;
  loading = false;
  articleRefs: GqlArticleRef[] = [];

  constructor(
    private readonly modalController: ModalController,
    private readonly changeDetectorRef: ChangeDetectorRef,
    private readonly apollo: Apollo
  ) {}

  ngOnInit() {
    this.loading = true;
    this.getRawArticles().subscribe((response: any) => {
      this.loading = false;
      this.articleRefs = response.data.articlesForFeedUrl.map(
        (article: GqlArticle) => {
          return {
            article,
          };
        }
      );
      this.changeDetectorRef.detectChanges();
    });
  }

  getRawArticles() {
    return this.apollo.query<any>({
      variables: {
        url: this.feed.feed_url,
      },
      query: gql`
        query ($url: String!) {
          articlesForFeedUrl(feedUrl: $url) {
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

  async subscribe() {
    return this.modalController.dismiss(this.feed);
  }
}
