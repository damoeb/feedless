import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
} from '@angular/core';
import { WizardStepId } from '../wizard/wizard.component';
import { FeedService } from '../../../services/feed.service';
import { ModalController } from '@ionic/angular';
import { GqlFeatureName, GqlVisibility } from '../../../../generated/graphql';
import { WizardHandler } from '../wizard-handler';
import {
  TransientOrExistingNativeFeed,
} from '../../../graphql/types';

export const assignNativeFeedToContext = async (
  feed: TransientOrExistingNativeFeed,
  handler: WizardHandler
) => {
  if (feed.transient) {
    await handler.updateContext({
      isCurrentStepValid: true,
      feed: {
        create: {
          nativeFeed: {
            feedUrl: feed.transient.url,
            title: feed.transient.title,
            description: feed.transient.description,
            visibility: GqlVisibility.IsPublic,
          },
        },
      },
    });
  }
  if (feed.existing) {
    await handler.updateContext({
      isCurrentStepValid: true,
      feed: {
        connect: {
          id: feed.existing.id,
        },
      },
    });
  }
  if (!feed.existing && !feed.transient) {
    throw new Error('expecting transient or existing feed');
  }
};

@Component({
  selector: 'app-wizard-source',
  templateUrl: './wizard-source.component.html',
  styleUrls: ['./wizard-source.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WizardSourceComponent implements OnInit {
  @Input()
  handler: WizardHandler;

  @Output()
  navigateTo: EventEmitter<WizardStepId> = new EventEmitter<WizardStepId>();
  feedFromPageChange = GqlFeatureName.GenFeedFromPageChange;

  effectiveWebsiteUrl: string | undefined;
  currentWebsiteUrl: string | undefined;

  constructor(
    private readonly feedService: FeedService,
    private readonly changeRef: ChangeDetectorRef,
    private readonly modalCtrl: ModalController
  ) {}

  async ngOnInit() {
    this.handler.onContextChange().subscribe(async (changes) => {
      if (this.currentWebsiteUrl !== this.handler.getDiscovery()?.websiteUrl) {
        this.currentWebsiteUrl = this.handler.getDiscovery().websiteUrl;
        this.effectiveWebsiteUrl = this.handler.getDiscovery().document.url;
        this.changeRef.detectChanges();
      }
      if (changes.busy) {
        this.changeRef.detectChanges();
      }
    });
  }

  async startFeedDiscoveryFlow(): Promise<void> {
    this.navigateTo.emit(WizardStepId.feeds);
  }

  async startPageChangedFlow(): Promise<void> {
    this.navigateTo.emit(WizardStepId.pageFragmentWatch);
  }

  closeModal(): Promise<boolean> {
    return this.modalCtrl.dismiss();
  }

  isFeed(): boolean {
    return (
      this.handler.hasMimeType('application/atom+xml') ||
      this.handler.hasMimeType('application/rss+xml') ||
      this.handler.hasMimeType('application/xml') ||
      this.handler.hasMimeType('text/xml')
    );
  }

  isWebsite(): boolean {
    return this.handler.hasMimeType('text/html');
  }

  isQuery(): boolean {
    return this.handler.hasMimeType('text/plain');
  }

  isSourceSupported(): boolean {
    return this.isFeed() || this.isWebsite();
  }

  mimetype(): string {
    return this.handler.getDiscovery().document.mimeType;
  }

  async startNativeFeedRefinementFlow(feed: TransientOrExistingNativeFeed) {
    await assignNativeFeedToContext(feed, this.handler);
    this.navigateTo.emit(WizardStepId.refineNativeFeed);
  }

  isRedirected(): boolean {
    return (
      this.getHostname(this.currentWebsiteUrl) !==
      this.getHostname(this.effectiveWebsiteUrl)
    );
  }

  getHostname(url: string): string {
    return new URL(url).hostname;
  }
}
