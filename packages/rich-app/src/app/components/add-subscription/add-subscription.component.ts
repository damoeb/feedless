import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit} from '@angular/core';
import {debounce, DebouncedFunc, isUndefined} from 'lodash';
import {ModalController} from '@ionic/angular';
import {SubscriptionService} from '../../services/subscription.service';
import {DiscoveredFeed, Subscription} from '../../../generated/graphql';
import {FeedComponent} from "../feed/feed.component";

@Component({
  selector: 'app-add-subscription',
  templateUrl: './add-subscription.component.html',
  styleUrls: ['./add-subscription.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AddSubscriptionComponent implements OnInit {
  @Input()
  subscription: Subscription|null;

  private queryString: string;
  loading: boolean;
  editMode: boolean;
  tags: string;
  private searchDebounced: DebouncedFunc<any>;
  resolvedFeeds: DiscoveredFeed[];

  constructor(private readonly modalController: ModalController,
              private readonly ref: ChangeDetectorRef,
              private readonly subscriptionService: SubscriptionService) { }

  ngOnInit() {
    this.editMode = !isUndefined(this.subscription);
    this.searchDebounced = debounce(this.search.bind(this), 300);
  }

  dismissModal() {
    return this.modalController.dismiss();
  }

  onQueryStringChange(queryString: string) {
    this.queryString = queryString;
    this.searchDebounced();
  }

  search() {
    this.loading = true;
    const sub = this.subscriptionService.discoverFeeds(this.queryString).valueChanges
      .subscribe(response => {
        this.resolvedFeeds = response.data.discoverFeedsByQuery;
        this.loading = false;
        this.ref.detectChanges();
        sub.unsubscribe();
      });
  }

  createSubscription() {
    return this.dismissModal();
  }

  updateSubscription() {
    return this.dismissModal();
  }

  async showFeed(feed: DiscoveredFeed) {
    const modal = await this.modalController.create({
      component: FeedComponent,
      componentProps: {
        feed
      },
    });

    await modal.present();
  }
}
