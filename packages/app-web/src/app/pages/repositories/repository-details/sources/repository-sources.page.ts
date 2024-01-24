import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { WebDocument } from '../../../../graphql/types';

@Component({
  selector: 'app-repository-sources-page',
  templateUrl: './repository-sources.page.html',
  styleUrls: ['./repository-sources.page.scss'],
})
export class RepositorySourcesPage implements OnInit, OnDestroy {
  entities: WebDocument[] = [];

  private subscriptions: Subscription[] = [];

  constructor(
    private readonly activatedRoute: ActivatedRoute,
  ) {
  }

  async ngOnInit() {
    this.subscriptions.push(
      this.activatedRoute.params.subscribe((params) => {
        // this.fetchSources(params.id);
      })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

}
