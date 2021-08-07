import { Component, Input, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { FeedService } from '../../services/feed.service';

@Component({
  selector: 'app-feed-details',
  templateUrl: './feed-details.component.html',
  styleUrls: ['./feed-details.component.scss'],
})
export class FeedDetailsComponent implements OnInit {
  @Input()
  feedId: string;

  constructor(
    private readonly modalController: ModalController,
    private readonly feedService: FeedService
  ) {}

  ngOnInit() {
    this.feedService.findById(this.feedId);
  }

  dismissModal() {
    return this.modalController.dismiss();
  }
}
