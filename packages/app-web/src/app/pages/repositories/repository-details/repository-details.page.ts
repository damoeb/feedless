import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { GqlVisibility } from '../../../../generated/graphql';
import { SourceSubscription } from '../../../graphql/types';
import { Subscription } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { SourceSubscriptionService } from '../../../services/source-subscription.service';
import { ProfileService } from '../../../services/profile.service';
import { ServerSettingsService } from '../../../services/server-settings.service';
import { ModalService } from '../../../services/modal.service';
import { WebDocumentService } from '../../../services/web-document.service';

@Component({
  selector: 'app-repository-details-page',
  templateUrl: './repository-details.page.html',
  styleUrls: ['./repository-details.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RepositoryDetailsPage implements OnInit, OnDestroy {
  loadingSource: boolean;
  source: SourceSubscription;
  readonly entityVisibility = GqlVisibility;
  feedUrl: string;

  private subscriptions: Subscription[] = [];

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly router: Router,
    private readonly sourceSubscriptionService: SourceSubscriptionService,
    private readonly profileService: ProfileService,
    private readonly serverSettings: ServerSettingsService,
    private readonly modalService: ModalService,
    private readonly webDocumentService: WebDocumentService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async ngOnInit() {
    this.subscriptions.push(
      this.activatedRoute.params.subscribe(async (params) => {
        this.loadingSource = true;
        try {
          this.source =
            await this.sourceSubscriptionService.getSubscriptionById(
              params.repositoryId,
            );
          this.changeRef.detectChanges();
        } catch (e) {
          console.error(e);
        } finally {
          this.loadingSource = false;
        }
        this.feedUrl = `${this.serverSettings.apiUrl}/feed/${params.repositoryId}`;
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
