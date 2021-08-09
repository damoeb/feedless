import { Component, Input, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { FeedService } from '../../services/feed.service';
import { GqlFeed } from '../../../generated/graphql';

@Component({
  selector: 'app-feed-details',
  templateUrl: './feed-details.component.html',
  styleUrls: ['./feed-details.component.scss'],
})
export class FeedDetailsComponent implements OnInit {
  @Input()
  feed: GqlFeed;

  constructor(
    private readonly modalController: ModalController,
    private readonly feedService: FeedService
  ) {}

  ngOnInit() {
    this.feedService.findById(this.feed.id);
  }

  dismissModal() {
    return this.modalController.dismiss();
  }
}
