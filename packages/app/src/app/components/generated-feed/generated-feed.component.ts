import { Component, Input, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';

import { GqlGenericFeedRule } from '../../../generated/graphql';

@Component({
  selector: 'app-generated-feed',
  templateUrl: './generated-feed.component.html',
  styleUrls: ['./generated-feed.component.scss'],
})
export class GeneratedFeedComponent implements OnInit {
  @Input()
  feed: GqlGenericFeedRule;

  constructor(private readonly modalController: ModalController) {}

  ngOnInit() {}

  dismissModal() {
    return this.modalController.dismiss();
  }

  async subscribe() {
    return this.modalController.dismiss(this.feed);
  }

}
