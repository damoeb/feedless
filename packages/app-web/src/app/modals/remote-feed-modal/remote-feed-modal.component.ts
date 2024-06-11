import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { RemoteFeed } from '../../graphql/types';

export interface RemoteFeedModalComponentProps {
  feedProvider: () => Promise<RemoteFeed>;
}

@Component({
  selector: 'app-remote-feed-modal',
  templateUrl: './remote-feed-modal.component.html',
  styleUrls: ['./remote-feed-modal.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RemoteFeedModalComponent
  implements RemoteFeedModalComponentProps, OnInit
{
  feedProvider: () => Promise<RemoteFeed>;
  protected feed: RemoteFeed;

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  closeModal() {
    return this.modalCtrl.dismiss();
  }

  async ngOnInit() {
    this.feed = await this.feedProvider();
    this.changeRef.detectChanges();
  }
}
