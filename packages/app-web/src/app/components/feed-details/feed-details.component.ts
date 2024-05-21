import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { GqlScrapeRequest, GqlVisibility } from '../../../generated/graphql';
import { FeedlessPlugin, GetElementType, Repository, SubscriptionSource, WebDocument } from '../../graphql/types';
import { GenerateFeedModalComponentProps, getScrapeRequest } from '../../modals/generate-feed-modal/generate-feed-modal.component';
import { ModalService } from '../../services/modal.service';
import { InfiniteScrollCustomEvent, ModalController, PopoverController } from '@ionic/angular';
import { FeedWithRequest, tagsToString } from '../feed-builder/feed-builder.component';
import { RepositoryService } from '../../services/repository.service';
import { ArrayElement } from '../../types';
import { BubbleColor } from '../bubble/bubble.component';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import { PluginService } from '../../services/plugin.service';
import { Router } from '@angular/router';
import { dateFormat, SessionService } from '../../services/session.service';
import { DocumentService } from '../../services/document.service';
import { ServerSettingsService } from '../../services/server-settings.service';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { first } from 'lodash-es';
import { Subscription } from 'rxjs';

type Enclosure = GetElementType<WebDocument['enclosures']>

@Component({
  selector: 'app-feed-details',
  templateUrl: './feed-details.component.html',
  styleUrls: ['./feed-details.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeedDetailsComponent implements OnInit, OnDestroy {
  @Input({ required: true })
  repository: Repository;

  @Input()
  track: boolean;

  protected pages: WebDocument[][] = [];
  protected feedUrl: string;

  private plugins: FeedlessPlugin[];

  protected readonly GqlVisibility = GqlVisibility;
  protected readonly dateFormat = dateFormat;
  showFullDescription: boolean = false;
  protected playDocument: WebDocument;
  private userId: string;
  private subscriptions: Subscription[] = [];

  constructor(
    private readonly modalService: ModalService,
    private readonly pluginService: PluginService,
    private readonly popoverCtrl: PopoverController,
    private readonly documentService: DocumentService,
    private readonly router: Router,
    private readonly domSanitizer: DomSanitizer,
    private readonly sessionService: SessionService,
    private readonly repositoryService: RepositoryService,
    private readonly serverSettingsService: ServerSettingsService,
    private readonly changeRef: ChangeDetectorRef,
    private readonly modalCtrl: ModalController,
  ) {}

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async ngOnInit() {
    dayjs.extend(relativeTime);
    this.feedUrl = `${this.serverSettingsService.gatewayUrl}/feed/${this.repository.id}/atom`;
    this.plugins = await this.pluginService.listPlugins();
    this.subscriptions.push(
      this.sessionService.getSession().subscribe((session) => {
        this.userId = session.user?.id;
      }),
    );
    await this.fetchNextPage();
    this.changeRef.detectChanges();
  }

  getPluginsOfRepository(repository: Repository) {
    if (!this.plugins) {
      return '';
    }
    return repository.plugins
      .map((plugin) => this.getPluginName(plugin.pluginId))
      .join(', ');
  }

  hostname(url: string): string {
    try {
      return new URL(url).hostname;
    } catch (e) {
      return 'Unknown';
    }
  }

  async editRepository() {
    const componentProps: GenerateFeedModalComponentProps = {
      repository: this.repository,
      modalTitle: `Customize ${this.repository.title}`,
    };
    await this.modalService.openFeedMetaEditor(componentProps);
    await this.popoverCtrl.dismiss();
  }

  hasErrors(): boolean {
    return (
      this.repository.sources.length === 0 ||
      this.repository.sources.some((s) => s.errornous)
    );
  }

  dismissModal() {
    this.modalCtrl.dismiss();
  }

  getHealthColorForSource(
    source: ArrayElement<Repository['sources']>,
  ): BubbleColor {
    if (source.errornous) {
      return 'red';
    } else {
      return 'green';
    }
  }

  hasEndReached(): boolean {
    return (
      this.pages.length > 0 && this.pages[this.pages.length - 1].length === 0
    );
  }

  async loadMore(event: any) {
    await this.fetchNextPage();
    await (event as InfiniteScrollCustomEvent).target.complete();
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
            id: this.repository.id,
          },
        },
      },
    });

    this.pages.push(documents);
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

  fromNow(futureTimestamp: number): string {
    const now = dayjs();
    const ts = dayjs(futureTimestamp);
    if (now.subtract(2, 'weeks').isAfter(ts)) {
      return ts.format('DD.MMMM YYYY');
    } else {
      return ts.toNow(true) + ' ago';
    }
  }

  async deleteRepository() {
    await this.repositoryService.deleteRepository({
      id: this.repository.id,
    });
    await this.popoverCtrl.dismiss();
    await this.router.navigateByUrl('/feeds');
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

  private getPluginName(pluginId: string) {
    return this.plugins.find((plugin) => plugin.id === pluginId)?.name;
  }

  stringifyTags(source: ArrayElement<Repository['sources']>) {
    return tagsToString(source.tags) || 'Add tags';
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

  async editTags(source: ArrayElement<Repository['sources']>) {
    const tags = await this.modalService.openTagModal({
      tags: source.tags || [],
    });
    this.repository = await this.repositoryService.updateRepository({
      where: {
        id: this.repository.id,
      },
      data: {
        sources: {
          update: [
            {
              where: {
                id: source.id,
              },
              data: {
                tags: {
                  set: tags,
                },
              },
            },
          ],
        },
      },
    });
    this.changeRef.detectChanges();
  }

  getTags(document: WebDocument) {
    return tagsToString(document.tags);
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

  hasAudioStream(document: WebDocument): boolean {
    return document.enclosures.some((e) => e.type.startsWith('audio/'));
  }

  playAudio(document: WebDocument): void {
    this.playDocument = document;
  }

  firstAudioStream(document: WebDocument): SafeResourceUrl {
    const audioStream = this.firstAudioEnclosure(document);
    if (audioStream) {
      return this.domSanitizer.bypassSecurityTrustResourceUrl(audioStream.url);
    }
  }

  private firstAudioEnclosure(document: WebDocument): Enclosure {
    return first(
      document.enclosures.filter((e) => e.type.startsWith('audio/')),
    );
  }

  isOwner(): boolean {
    return this.repository.ownerId === this.userId;
  }

  getDocumentUrl(document: WebDocument): string {
    if (this.track) {
      return `${this.serverSettingsService.gatewayUrl}/article/${document.id}`;
    } else {
      return document.url;
    }
  }

  firstAudioLength(document: WebDocument): string {
    const audioStream = this.firstAudioEnclosure(document);
    if (audioStream) {
      return `${parseInt(`${audioStream.duration / 60}`)}  Min.`;
    }
  }
}
