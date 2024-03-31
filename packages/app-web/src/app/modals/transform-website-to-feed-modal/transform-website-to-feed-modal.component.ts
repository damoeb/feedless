import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input } from '@angular/core';
import { GqlNativeFeed, GqlScrapeRequestInput, GqlTransientGenericFeed } from '../../../generated/graphql';
import { ScrapeResponse } from '../../graphql/types';
import { ModalController } from '@ionic/angular';
import { ComponentStatus } from '../../components/transform-website-to-feed/transform-website-to-feed.component';

export interface NativeOrGenericFeed {
  genericFeed?: GqlTransientGenericFeed;
  nativeFeed?: GqlNativeFeed;
}

export interface TransformWebsiteToFeedModalComponentProps {
  scrapeRequest: GqlScrapeRequestInput;
  scrapeResponse: ScrapeResponse;
  feed: NativeOrGenericFeed;
}

@Component({
  selector: 'app-transform-website-to-feed-modal',
  templateUrl: './transform-website-to-feed-modal.component.html',
  styleUrls: ['./transform-website-to-feed-modal.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TransformWebsiteToFeedModalComponent
  implements TransformWebsiteToFeedModalComponentProps
{
  @Input({ required: true })
  scrapeRequest: GqlScrapeRequestInput;

  @Input({ required: true })
  scrapeResponse: ScrapeResponse;

  @Input()
  feed: NativeOrGenericFeed;

  selectedFeed: NativeOrGenericFeed;
  busy = false;
  protected isValid = false;

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly modalCtrl: ModalController,
  ) {}

  dismissModal() {
    return this.modalCtrl.dismiss();
  }

  applyChanges() {
    return this.modalCtrl.dismiss(this.selectedFeed);
  }

  handleStatusChange(status: ComponentStatus) {
    this.isValid = status == 'valid';
  }

  handleFeedChange(feed: NativeOrGenericFeed) {
    this.selectedFeed = feed;
  }
}
