import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { RepositoryService } from '../../../../services/repository.service';
import { Repository } from '../../../../graphql/types';

@Component({
  selector: 'app-repository-settings-page',
  templateUrl: './repository-settings-page.component.html',
  styleUrls: ['./repository-settings-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RepositorySettingsPage implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];
  private repository: Repository;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly repositoryService: RepositoryService,
  ) {}

  async ngOnInit() {
    const repositoryId = this.activatedRoute.snapshot.params.repositoryId;
    this.repository =
      await this.repositoryService.getRepositoryById(repositoryId);

    this.subscriptions.push(
      this.activatedRoute.params.subscribe((params) => {}),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
