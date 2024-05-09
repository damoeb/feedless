import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { GqlVisibility } from '../../../../generated/graphql';
import { Repository } from '../../../graphql/types';
import { Subscription } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { RepositoryService } from '../../../services/repository.service';
import { ServerSettingsService } from '../../../services/server-settings.service';

@Component({
  selector: 'app-repository-details-page',
  templateUrl: './repository-details.page.html',
  styleUrls: ['./repository-details.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RepositoryDetailsPage implements OnInit, OnDestroy {
  loadingSource: boolean;
  repository: Repository;
  readonly entityVisibility = GqlVisibility;
  feedUrl: string;

  private subscriptions: Subscription[] = [];

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly repositoryService: RepositoryService,
    private readonly serverSettings: ServerSettingsService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async ngOnInit() {
    this.subscriptions.push(
      this.activatedRoute.params.subscribe(async (params) => {
        this.loadingSource = true;
        try {
          this.repository = await this.repositoryService.getRepositoryById(
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
