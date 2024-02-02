import {
  ChangeDetectionStrategy, ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { FeedlessPlugin, FeedlessPluginExecution, SourceSubscription } from '../../../../graphql/types';
import { PluginService } from '../../../../services/plugin.service';
import { SourceSubscriptionService } from '../../../../services/source-subscription.service';

@Component({
  selector: 'app-repository-data-page',
  templateUrl: './repository-plugins.page.html',
  styleUrls: ['./repository-plugins.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RepositoryPluginsPage implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];
  private allPlugins: FeedlessPlugin[];
  repository: SourceSubscription;

  activePlugins: Array<FeedlessPluginExecution & FeedlessPlugin>;
  inactivePlugins: FeedlessPlugin[];

  constructor(private readonly activatedRoute: ActivatedRoute,
              private readonly pluginService: PluginService,
              private readonly sourceSubscriptionService: SourceSubscriptionService,
              private readonly changeRef: ChangeDetectorRef) {}

  async ngOnInit() {
    this.allPlugins = await this.pluginService.listPlugins();
    const repositoryId = this.activatedRoute.snapshot.params.repositoryId;
    const repository = await this.sourceSubscriptionService.getSubscriptionById(repositoryId);
    this.activePlugins = repository.plugins.map(plugin => {
      return {
        ...plugin,
        ...this.allPlugins.find(otherPlugin => otherPlugin.id === plugin.pluginId)
      }
    });

    this.inactivePlugins = this.allPlugins
      .filter(plugin => plugin.listed)
      .filter(plugin => this.activePlugins.every(activePlugin => activePlugin.id != plugin.id))

    this.changeRef.detectChanges();
    this.subscriptions.push(
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
