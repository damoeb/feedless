import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import { WizardStepId } from '../wizard/wizard.component';
import {
  BasicNativeFeed,
  FeedService,
  TransientNativeFeed,
} from '../../../services/feed.service';
import { ModalController } from '@ionic/angular';
import { GqlFeatureName, GqlVisibility } from '../../../../generated/graphql';
import { WizardHandler } from '../wizard-handler';
import { Pagination } from '../../../services/pagination.service';

@Component({
  selector: 'app-wizard-source',
  templateUrl: './wizard-source.component.html',
  styleUrls: ['./wizard-source.component.scss'],
})
export class WizardSourceComponent implements OnInit, OnChanges {
  @Input()
  handler: WizardHandler;

  @Output()
  navigateTo: EventEmitter<WizardStepId> = new EventEmitter<WizardStepId>();
  feedFromPageChange = GqlFeatureName.GenFeedFromPageChange;
  matchingFeeds: BasicNativeFeed[] = [];

  private currentWebsiteUrl: string | undefined;
  private pagination: Pagination;

  constructor(
    private readonly feedService: FeedService,
    private readonly modalCtrl: ModalController
  ) {}

  async ngOnChanges(changes: SimpleChanges): Promise<void> {
    console.log('change');
    if (this.currentWebsiteUrl !== this.handler.getDiscovery()?.websiteUrl) {
      this.currentWebsiteUrl = this.handler.getDiscovery().websiteUrl;
      await this.searchNativeFeeds();
    }
  }

  ngOnInit() {}

  isWebsite(): boolean {
    return this.handler.hasMimeType('text/html');
  }

  async startFeedDiscoveryFlow(): Promise<void> {
    this.navigateTo.emit(WizardStepId.feeds);
  }

  async startPageChangedFlow(): Promise<void> {
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

  async startExistingNativeFeedRefinementFlow(nativeFeed: BasicNativeFeed) {
    await this.handler.updateContext({
      feed: {
        connect: {
          id: nativeFeed.id,
        },
      },
    });
    this.navigateTo.emit(WizardStepId.refineNativeFeed);
  }

  async startCreateNativeFeedRefinementFlow(nativeFeed: TransientNativeFeed) {
    await this.handler.updateContext({
      feed: {
        create: {
          nativeFeed: {
            feedUrl: nativeFeed.url,
            title: nativeFeed.title,
            description: nativeFeed.description,
            autoRelease: true,
            harvestItems: false,
            harvestSiteWithPrerender: false,
            visibility: GqlVisibility.IsProtected,
          },
        },
      },
    });
    this.navigateTo.emit(WizardStepId.refineNativeFeed);
  }

  hostname(): string {
    return new URL(this.handler.getDiscovery().websiteUrl).hostname;
  }

  private async searchNativeFeeds() {
    await this.feedService
      .searchNativeFeeds({
        where: {
          query: this.currentWebsiteUrl,
        },
        page: this.pagination ? this.pagination.page + 1 : 0,
      })
      .then((response) => {
        this.matchingFeeds.push(...response.nativeFeeds);
        this.pagination = response.pagination;
      });
  }
}
