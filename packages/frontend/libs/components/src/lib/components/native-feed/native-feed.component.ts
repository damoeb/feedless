import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, input, OnInit } from '@angular/core';
import { ScrapeService } from '@feedless/services';
import { GqlFeedlessPlugins, Record } from '@feedless/graphql-api';
import { last } from 'lodash-es';

import { IonItem, IonLabel, IonList, IonSpinner } from '@ionic/angular/standalone';
import { RemoteFeedItemComponent } from '../remote-feed-item/remote-feed-item.component';

@Component({
  selector: 'app-native-feed',
  templateUrl: './native-feed.component.html',
  styleUrls: ['./native-feed.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonItem, IonLabel, IonSpinner, IonList, RemoteFeedItemComponent],
  standalone: true,
})
export class NativeFeedComponent implements OnInit {
  private readonly scrapeService = inject(ScrapeService);
  private readonly changeRef = inject(ChangeDetectorRef);

  readonly feedUrl = input.required<string>();
  readonly title = input.required<string>();
  readonly showTitle = input(true);

  loading: boolean;
  feedItems: Record[];
  errorMessage: string;

  ngOnInit() {
    return this.refresh();
  }

  async refresh() {
    await this.fetch(this.feedUrl());
  }

  private async fetch(nativeFeedUrl: string): Promise<void> {
    this.loading = true;
    this.feedItems = [];
    this.changeRef.detectChanges();
    try {
      this.feedItems = await this.scrapeService
        .scrape({
          title: 'Remote Feed',
          flow: {
            sequence: [
              { fetch: { get: { url: { literal: nativeFeedUrl } } } },
              {
                execute: {
                  pluginId: GqlFeedlessPlugins.OrgFeedlessFeed,
                  params: {},
                },
              },
            ],
          },
        })
        .then((response) => last(response.outputs).response.extract.items);
    } catch (e: any) {
      this.errorMessage = e?.message;
    }
    this.loading = false;
    this.changeRef.detectChanges();
  }
}
