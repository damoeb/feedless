import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { WizardFlow, WizardStepId } from '../wizard/wizard.component';
import { FeedService } from '../../../services/feed.service';
import { ModalController } from '@ionic/angular';
import { GqlFeatureName } from '../../../../generated/graphql';
import { WizardHandler } from '../wizard-handler';

@Component({
  selector: 'app-wizard-source',
  templateUrl: './wizard-source.component.html',
  styleUrls: ['./wizard-source.component.scss'],
})
export class WizardSourceComponent implements OnInit {
  @Input()
  handler: WizardHandler;

  @Output()
  navigateTo: EventEmitter<WizardStepId> = new EventEmitter<WizardStepId>();
  feedFromPageChange = GqlFeatureName.GenFeedFromPageChange;

  constructor(
    private readonly feedService: FeedService,
    private readonly modalCtrl: ModalController
  ) {}

  ngOnInit() {}

  isWebsite(): boolean {
    return this.handler.hasMimeType('text/html');
  }

  async startFeedDiscoveryFlow(): Promise<void> {
    await this.handler.updateContext({ wizardFlow: WizardFlow.feedFromWebsite });
    this.navigateTo.emit(WizardStepId.feeds);
  }

  async startPageChangedFlow(): Promise<void> {
    await this.handler.updateContext({ wizardFlow: WizardFlow.feedFromPageChange });
    this.navigateTo.emit(WizardStepId.pageChange);
  }

  closeModal(): Promise<boolean> {
    return this.modalCtrl.dismiss();
  }

  isFeed(): boolean {
    return this.handler.hasMimeType('application/atom+xml');
  }

  isSourceSupported(): boolean {
    return this.isFeed() || this.isWebsite();
  }

  mimetype(): string {
    return this.handler.getDiscovery().document.mimeType;
  }

  async startFeedRefineryFlow(): Promise<void> {
    const feedUrl = this.handler.getDiscovery().nativeFeeds[0].url;
    await this.handler.updateContext({
      wizardFlow: WizardFlow.feedFromFeed,
      feedUrl
    });
    this.navigateTo.emit(WizardStepId.refineNativeFeed);
  }
}
