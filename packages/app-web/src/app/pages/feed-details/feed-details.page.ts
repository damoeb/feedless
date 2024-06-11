import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { Repository, WebDocument } from '../../graphql/types';
import { RepositoryService } from '../../services/repository.service';
import { dateFormat } from '../../services/session.service';
import { ServerConfigService } from '../../services/server-config.service';
import { Title } from '@angular/platform-browser';
import { relativeTimeOrElse } from '../../components/agents/agents.component';

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
  private repositoryId: string;

  protected errorMessage: string;

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly activatedRoute: ActivatedRoute,
    private readonly titleService: Title,
    private readonly serverConfig: ServerConfigService,
    private readonly repositoryService: RepositoryService,
  ) {}

  async ngOnInit() {
    this.subscriptions.push(
      this.activatedRoute.params.subscribe((params) => {
        if (params.feedId) {
          this.repositoryId = params.feedId;
          this.fetch();
        }
      }),
    );
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
      this.feedUrl = `${this.serverConfig.gatewayUrl}/feed/${this.repository.id}/atom`;
    } catch (e) {
      this.errorMessage = e.message;
    }

    this.busy = false;
    this.changeRef.detectChanges();
  }

  fromNow = relativeTimeOrElse;
}
