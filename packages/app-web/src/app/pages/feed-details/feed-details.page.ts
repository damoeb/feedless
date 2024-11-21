import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Subscription } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { RepositoryFull } from '../../graphql/types';
import { RepositoryService } from '../../services/repository.service';
import { dateFormat } from '../../services/session.service';
import { ServerConfigService } from '../../services/server-config.service';
import { relativeTimeOrElse } from '../../components/agents/agents.component';
import { AppConfigService } from '../../services/app-config.service';

@Component({
  selector: 'app-feed-details-page',
  templateUrl: './feed-details.page.html',
  styleUrls: ['./feed-details.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeedDetailsPage implements OnInit, OnDestroy {
  busy = true;
  repository: RepositoryFull;
  feedUrl: string;
  fromNow = relativeTimeOrElse;
  protected readonly dateFormat = dateFormat;
  protected errorMessage: string;
  private subscriptions: Subscription[] = [];
  private diffImageUrl: string;
  private repositoryId: string;

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly activatedRoute: ActivatedRoute,
    private readonly appConfig: AppConfigService,
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
        {
          cursor: {
            page: 0,
          },
        },
      );
      this.appConfig.setPageTitle(this.repository.title);
      this.feedUrl = `${this.serverConfig.apiUrl}/f/${this.repository.id}/atom`;
    } catch (e: any) {
      this.errorMessage = e?.message;
    }

    this.busy = false;
    this.changeRef.detectChanges();
  }
}
