import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { SourceSubscriptionService } from '../../../../services/source-subscription.service';
import { SourceSubscription } from '../../../../graphql/types';

@Component({
  selector: 'app-repository-settings-page',
  templateUrl: './repository-settings-page.component.html',
  styleUrls: ['./repository-settings-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RepositorySettingsPage implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];
  private repository: SourceSubscription;

  constructor(private readonly activatedRoute: ActivatedRoute,
              private readonly sourceSubscriptionService: SourceSubscriptionService) {}

  async ngOnInit() {
    const repositoryId = this.activatedRoute.snapshot.params.repositoryId;
    this.repository = await this.sourceSubscriptionService.getSubscriptionById(repositoryId);

    this.subscriptions.push(
      this.activatedRoute.params.subscribe((params) => {}),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
