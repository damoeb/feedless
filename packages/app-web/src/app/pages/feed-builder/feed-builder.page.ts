import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Subscription } from 'rxjs';
import {
  AppConfigService,
  ProductConfig,
} from '../../services/app-config.service';
import {
  GenerateFeedModalComponentProps,
  getScrapeRequest,
} from '../../modals/generate-feed-modal/generate-feed-modal.component';
import {
  FeedWithRequest,
  NativeOrGenericFeed,
} from '../../components/feed-builder/feed-builder.component';
import { ModalService } from '../../services/modal.service';
import {
  GqlFeedlessPlugins,
  GqlScrapeRequest,
  GqlSourceInput,
} from '../../../generated/graphql';
import { getFirstFetchUrlLiteral } from '../../utils';
import { Repository } from '../../graphql/types';
import { ServerConfigService } from '../../services/server-config.service';
import { Title } from '@angular/platform-browser';

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
    });
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
    );
  }

  private async handleSource(
    title: string,
    description: string,
    source: GqlSourceInput,
  ) {
    const componentProps: GenerateFeedModalComponentProps = {
      repository: {
        title,
        description,
        plugins: [],
        sources: [source],
      } as any,
    };
    await this.modalService.openFeedMetaEditor(componentProps);
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
