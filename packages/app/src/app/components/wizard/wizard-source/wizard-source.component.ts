import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import {
  WizardContext,
  WizardMode,
  WizardStepId,
} from '../wizard/wizard.component';
import { FeedService } from '../../../services/feed.service';
import { ModalController } from '@ionic/angular';
import { GqlFeatureName } from '../../../../generated/graphql';

@Component({
  selector: 'app-wizard-source',
  templateUrl: './wizard-source.component.html',
  styleUrls: ['./wizard-source.component.scss'],
})
export class WizardSourceComponent implements OnInit {
  @Input()
  context: WizardContext;

  @Output()
  updateContext: EventEmitter<Partial<WizardContext>> = new EventEmitter<
    Partial<WizardContext>
  >();
  @Output()
  navigateTo: EventEmitter<WizardStepId> = new EventEmitter<WizardStepId>();
  feedFromPageChange = GqlFeatureName.GenFeedFromPageChange;

  constructor(
    private readonly feedService: FeedService,
    private readonly modalCtrl: ModalController
  ) {}

  ngOnInit() {}

  isWebsite(): boolean {
    return this.hasMimeType('text/html');
  }

  startFeedDiscoveryFlow() {
    this.updateContext.emit({ wizardMode: WizardMode.feedFromWebsite });
    this.navigateTo.emit(WizardStepId.feeds);
  }

  startPageChangedFlow() {
    this.updateContext.emit({ wizardMode: WizardMode.feedFromPageChange });
    this.navigateTo.emit(WizardStepId.pageChange);
  }

  closeModal() {
    return this.modalCtrl.dismiss();
  }

  isFeed() {
    return (
      this.context.discovery &&
      !this.context.discovery.failed &&
      this.context.discovery.document.mimeType.startsWith(
        'application/atom+xml'
      )
    );
  }

  isSourceSupported(): boolean {
    return this.isFeed() || this.isWebsite();
  }

  mimetype(): string {
    return this.context.discovery.document.mimeType;
  }

  startFeedRefineryFlow() {
    this.updateContext.emit({
      wizardMode: WizardMode.feedFromFeed,
      feedUrl: this.context.discovery.nativeFeeds[0].url,
    });
    this.navigateTo.emit(WizardStepId.refineNativeFeed);
  }

  private hasMimeType(mime: string): boolean {
    return (
      this.context.discovery &&
      !this.context.discovery.failed &&
      this.context.discovery.document.mimeType.startsWith(mime)
    );
  }
}
