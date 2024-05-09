import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Subscription } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { DocumentService } from '../../../services/document.service';
import {
  FeedlessPlugin,
  Repository,
  SubscriptionSource,
  WebDocument,
} from '../../../graphql/types';
import { RepositoryService } from '../../../services/repository.service';
import { dateFormat } from '../../../services/session.service';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import { ServerSettingsService } from '../../../services/server-settings.service';
import { ModalService } from '../../../services/modal.service';
import { FeedWithRequest } from '../../../components/feed-builder/feed-builder.component';
import { GqlScrapeRequest, GqlVisibility } from '../../../../generated/graphql';
import {
  GenerateFeedModalComponentProps,
  getScrapeRequest,
} from '../../../modals/generate-feed-modal/generate-feed-modal.component';
import { ModalController } from '@ionic/angular';
import { BubbleColor } from '../../../components/bubble/bubble.component';
import { ArrayElement } from '../../../types';
import { PluginService } from '../../../services/plugin.service';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-feed-details-page',
  templateUrl: './feed-details.page.html',
  styleUrls: ['./feed-details.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeedDetailsPage implements OnInit, OnDestroy {
  busy = true;
  pages: WebDocument[][] = [];
  private subscriptions: Subscription[] = [];
  private diffImageUrl: string;
  repository: Repository;

  protected readonly dateFormat = dateFormat;
  feedUrl: string;
  private plugins: FeedlessPlugin[];
  private repositoryId: string;

  protected readonly GqlVisibility = GqlVisibility;
  showFullDescription = false;
  showImages = false;
  showFullArticle = false;
  protected errorMessage: string;

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly activatedRoute: ActivatedRoute,
    private readonly pluginService: PluginService,
    private readonly modalCtrl: ModalController,
    private readonly router: Router,
    private readonly modalService: ModalService,
    private readonly titleService: Title,
    private readonly serverSettingsService: ServerSettingsService,
    private readonly repositoryService: RepositoryService,
    private readonly documentService: DocumentService,
  ) {}

  async ngOnInit() {
    dayjs.extend(relativeTime);
    this.subscriptions.push(
      this.activatedRoute.params.subscribe((params) => {
        if (params.feedId) {
          this.repositoryId = params.feedId;
          this.fetch();
        }
      }),
    );
    this.plugins = await this.pluginService.listPlugins();
    this.changeRef.detectChanges();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
    URL.revokeObjectURL(this.diffImageUrl);
  }

  private async fetch() {
    this.busy = true;
    this.changeRef.detectChanges();

    try {

      this.repository = await this.repositoryService.getRepositoryById(
        this.repositoryId,
      );
      this.titleService.setTitle(this.repository.title);
      this.feedUrl = `${this.serverSettingsService.gatewayUrl}/feed/${this.repository.id}/atom`;

      await this.fetchNextPage();

    } catch (e) {
      this.errorMessage = e.message
    }

    this.busy = false;
    this.changeRef.detectChanges();
  }

  private async fetchNextPage() {
    const documents = await this.documentService.findAllByStreamId({
      cursor: {
        page: this.pages.length,
        pageSize: 10,
      },
      where: {
        repository: {
          where: {
            id: this.repositoryId,
          },
        },
      },
    });

    this.pages.push(documents);
  }

  fromNow(futureTimestamp: number): string {
    return dayjs(futureTimestamp).toNow(true);
  }

  deleteWebDocument(document: WebDocument) {
    return this.documentService.removeById({
      where: {
        id: document.id,
      },
    });
  }

  getHealthColorForSource(
    source: ArrayElement<Repository['sources']>,
  ): BubbleColor {
    if (source.errornous) {
      return 'red';
    } else {
      return 'blue';
    }
  }
  async editSource(source: SubscriptionSource = null) {
    await this.modalService.openFeedBuilder(
      {
        scrapeRequest: source as any,
      },
      async (data: FeedWithRequest) => {
        if (data) {
          this.repository = await this.repositoryService.updateRepository({
            where: {
              id: this.repository.id,
            },
            data: {
              sources: {
                add: [
                  getScrapeRequest(
                    data.feed,
                    data.scrapeRequest as GqlScrapeRequest,
                  ),
                ],
                remove: source ? [source.id] : [],
              },
            },
          });
          this.changeRef.detectChanges();
        }
      },
    );
  }

  async deleteSource(source: SubscriptionSource) {
    console.log('deleteSource', source);
    this.repository = await this.repositoryService.updateRepository({
      where: {
        id: this.repository.id,
      },
      data: {
        sources: {
          remove: [source.id],
        },
      },
    });
    this.changeRef.detectChanges();
  }

  dismissModal() {
    this.modalCtrl.dismiss();
  }

  hostname(url: string): string {
    return new URL(url).hostname;
  }

  async editRepository() {
    const componentProps: GenerateFeedModalComponentProps = {
      repository: this.repository,
      modalTitle: `Customize ${this.repository.title}`,
    };
    await this.modalService.openFeedMetaEditor(componentProps);
  }

  async deleteRepository() {
    await this.repositoryService.deleteRepository({
      id: this.repository.id,
    });
    await this.router.navigateByUrl('/feeds');
  }

  getRetentionStrategy(): string {
    if (
      this.repository.retention.maxAgeDays ||
      this.repository.retention.maxItems
    ) {
      if (
        this.repository.retention.maxAgeDays &&
        this.repository.retention.maxItems
      ) {
        return `${this.repository.retention.maxAgeDays} days, ${this.repository.retention.maxItems} items`;
      } else {
        if (this.repository.retention.maxAgeDays) {
          return `${this.repository.retention.maxAgeDays} days`;
        } else {
          return `${this.repository.retention.maxItems} items`;
        }
      }
    } else {
      return 'Auto';
    }
  }

  hasErrors(): boolean {
    return (
      this.repository.sources.length === 0 ||
      this.repository.sources.some((s) => s.errornous)
    );
  }

  getPluginsOfSource(source: ArrayElement<Repository['sources']>): string {
    if (!this.plugins) {
      return '';
    }
    return source.emit
      .flatMap(
        (emit) =>
          emit.selectorBased?.expose.transformers.flatMap((transformer) =>
            this.getPluginName(transformer.pluginId),
          ),
      )
      .join(', ');
  }

  getPluginsOfSubscription(subscription: Repository) {
    if (!this.plugins) {
      return '';
    }
    return subscription.plugins
      .map((plugin) => this.getPluginName(plugin.pluginId))
      .join(', ');
  }

  private getPluginName(pluginId: string) {
    return this.plugins.find((plugin) => plugin.id === pluginId)?.name;
  }

  hasEndReached(): boolean {
    return (
      this.pages.length > 0 && this.pages[this.pages.length - 1].length === 0
    );
  }

  async loadMore() {
    await this.fetchNextPage();
    // await (event as InfiniteScrollCustomEvent).target.complete();
  }
}
