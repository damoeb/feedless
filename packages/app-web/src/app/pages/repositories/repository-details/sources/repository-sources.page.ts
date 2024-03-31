import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { SubscriptionSource } from '../../../../graphql/types';
import { SourceSubscriptionService } from '../../../../services/source-subscription.service';
import { ModalController } from '@ionic/angular';

@Component({
  selector: 'app-repository-sources-page',
  templateUrl: './repository-sources.page.html',
  styleUrls: ['./repository-sources.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RepositorySourcesPage implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];
  sources: SubscriptionSource[];

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly changeRef: ChangeDetectorRef,
    private readonly modalCtrl: ModalController,
    private readonly sourceSubscriptionService: SourceSubscriptionService,
  ) {}

  async ngOnInit() {
    const repositoryId = this.activatedRoute.snapshot.params.repositoryId;
    const repository =
      await this.sourceSubscriptionService.getSubscriptionById(repositoryId);
    this.sources = repository.sources;
    this.changeRef.detectChanges();
    this.subscriptions.push();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  addSource() {}
}
