import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { AppConfigService, ProductConfig } from '../../services/app-config.service';
import { GenerateFeedModalComponentProps, getScrapeRequest } from '../../modals/generate-feed-modal/generate-feed-modal.component';
import { FeedWithRequest, NativeOrGenericFeed } from '../../components/feed-builder/feed-builder.component';
import { ModalService } from '../../services/modal.service';
import { GqlFeedlessPlugins, GqlScrapeRequest, GqlSourceInput, GqlVisibility } from '../../../generated/graphql';
import { getFirstFetchUrlLiteral } from '../../utils';
import { Repository } from '../../graphql/types';
import { ServerConfigService } from '../../services/server-config.service';
import { Title } from '@angular/platform-browser';
import { environment } from '../../../environments/environment';
import { RepositoryService } from '../../services/repository.service';
import { Router } from '@angular/router';

export const DEFAULT_FETCH_CRON: string = '0 0 0 * * *';

@Component({
  selector: 'app-feed-builder-page',
  templateUrl: './feed-builder.page.html',
  styleUrls: ['./feed-builder.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeedBuilderPage implements OnInit, OnDestroy {
  loading = false;
  productConfig: ProductConfig;
  private subscriptions: Subscription[] = [];

  constructor(
    private readonly appConfigService: AppConfigService,
    private readonly modalService: ModalService,
    private readonly titleService: Title,
    private readonly repositoryService: RepositoryService,
    private readonly router: Router,
    private readonly serverConfig: ServerConfigService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async ngOnInit() {
    this.titleService.setTitle('Feed Builder');
    this.subscriptions.push(
      this.appConfigService
        .getActiveProductConfigChange()
        .subscribe((productConfig) => {
          this.productConfig = productConfig;
          this.changeRef.detectChanges();
        }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async handleRepository(repository: Repository) {
    const url = `${this.serverConfig.gatewayUrl}/f/${repository.id}/atom?skey=${repository.shareKey}`;
    await this.handleSource(`Remix ${repository.title}`, '', {
      title: `From ${url}`,
      flow: {
        sequence: [
          {
            fetch: {
              get: {
                url: {
                  literal: url,
                },
              },
            },
          },
          {
            execute: {
              pluginId: GqlFeedlessPlugins.OrgFeedlessFeed,
              params: {},
            },
          },
        ],
      },
    }, true);
  }

  async handleFeed(feed: FeedWithRequest) {
    const { title, description } = this.getFeedData(
      feed.feed,
      getFirstFetchUrlLiteral(feed.scrapeRequest.flow.sequence),
    );
    await this.handleSource(
      title,
      description,
      getScrapeRequest(feed.feed, feed.scrapeRequest as GqlScrapeRequest),
      feed.refine
    );
  }

  private async handleSource(
    title: string,
    description: string,
    source: GqlSourceInput,
    refine: boolean
  ) {
    if (refine) {
      const componentProps: GenerateFeedModalComponentProps = {
        repository: {
          title,
          description,
          plugins: [],
          sources: [source],
        } as any
      };
      await this.modalService.openFeedMetaEditor(componentProps);
    } else {
      const repositories = await this.repositoryService.createRepositories({
        repositories: [
          {
            product: environment.product,
            sources: [source] as GqlSourceInput[],
            sinkOptions: {
              title,
              refreshCron: DEFAULT_FETCH_CRON,
              withShareKey: true,
              description,
              visibility: GqlVisibility.IsPrivate,
              plugins: [],
            },
          },
        ],
      });

      const firstRepository = repositories[0];
      await this.router.navigateByUrl(`/feeds/${firstRepository.id}`);
    }
  }

  private getFeedData(feed: NativeOrGenericFeed, urlString: string) {
    if (feed.nativeFeed) {
      return {
        title: feed.nativeFeed.title,
        description: `Source: ${feed.nativeFeed.feedUrl}`,
      };
    } else {
      const url = new URL(urlString);
      return {
        title: `Feed from ${url.host}${url.pathname}`,
        description: `Source: ${url}`,
      };
    }
  }
}
