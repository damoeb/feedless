import { Component, Input, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';

import {
  FieldWrapper,
  GqlArticle,
  GqlProxyArticle,
  GqlProxyFeed,
} from '../../../generated/graphql';

@Component({
  selector: 'app-generated-feed',
  templateUrl: './generated-feed.component.html',
  styleUrls: ['./generated-feed.component.scss'],
})
export class GeneratedFeedComponent implements OnInit {
  @Input()
  feed: GqlProxyFeed;

  constructor(private readonly modalController: ModalController) {}

  ngOnInit() {}

  dismissModal() {
    return this.modalController.dismiss();
  }

  async subscribe() {
    return this.modalController.dismiss(this.feed);
  }

  toArticle(article: FieldWrapper<GqlProxyArticle>): GqlArticle {
    return {
      date_published: new Date(),
      url: article.link,
      title: article.title,
      // tags?: Maybe<FieldWrapper<Scalars['JSON']>>;
      content_raw: article.text,
    } as GqlArticle;
  }
}
