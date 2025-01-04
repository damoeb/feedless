import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Subscription } from 'rxjs';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { RepositoryFull } from '../../graphql/types';
import { RepositoryService } from '../../services/repository.service';
import { dateFormat } from '../../services/session.service';
import { ServerConfigService } from '../../services/server-config.service';
import { relativeTimeOrElse } from '../../components/agents/agents.component';
import { AppConfigService } from '../../services/app-config.service';
import { FeedlessHeaderComponent } from '../../components/feedless-header/feedless-header.component';

import {
  IonBreadcrumb,
  IonBreadcrumbs,
  IonButton,
  IonContent,
  IonHeader,
  IonItem,
  IonSpinner,
  IonText,
  IonToolbar,
} from '@ionic/angular/standalone';
import { FeedDetailsComponent } from '../../components/feed-details/feed-details.component';

@Component({
  selector: 'app-feed-details-page',
  templateUrl: './feed-details.page.html',
  styleUrls: ['./feed-details.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IonHeader,
    IonToolbar,
    IonText,
    IonButton,
    IonContent,
    IonBreadcrumbs,
    IonBreadcrumb,
    RouterLink,
    IonSpinner,
    IonItem,
    FeedDetailsComponent,
  ],
  standalone: true,
})
export class FeedDetailsPage implements OnInit, OnDestroy {
  private readonly changeRef = inject(ChangeDetectorRef);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly appConfig = inject(AppConfigService);
  private readonly serverConfig = inject(ServerConfigService);
  private readonly repositoryService = inject(RepositoryService);

  busy = true;
  repository: RepositoryFull;
  feedUrl: string;
  fromNow = relativeTimeOrElse;
  protected readonly dateFormat = dateFormat;
  protected errorMessage: string;
  private subscriptions: Subscription[] = [];
  private diffImageUrl: string;
  private repositoryId: string;

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
          page: 0,
          pageSize: 0,
        },
        null,
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
