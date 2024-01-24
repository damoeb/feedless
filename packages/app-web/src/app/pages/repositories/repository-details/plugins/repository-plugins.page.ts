import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-repository-data-page',
  templateUrl: './repository-plugins.page.html',
  styleUrls: ['./repository-plugins.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RepositoryPluginsPage implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];

  constructor(
    private readonly activatedRoute: ActivatedRoute,
  ) {
  }

  async ngOnInit() {
    this.subscriptions.push(
      this.activatedRoute.params.subscribe((params) => {
      })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

}
