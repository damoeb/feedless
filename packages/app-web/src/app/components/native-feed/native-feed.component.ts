import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnInit,
} from '@angular/core';
import { ScrapeService } from '../../services/scrape.service';
import { GqlFeedlessPlugins } from '../../../generated/graphql';
import { last } from 'lodash-es';
import { Record } from '../../graphql/types';

@Component({
  selector: 'app-native-feed',
  templateUrl: './native-feed.component.html',
  styleUrls: ['./native-feed.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NativeFeedComponent implements OnInit {
  @Input({ required: true })
  feedUrl: string;
  @Input({ required: true })
  title = 'Feed Preview';
  @Input()
  showTitle = true;

  loading: boolean;
  feedItems: Record[];
  errorMessage: string;

  constructor(
    private readonly scrapeService: ScrapeService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  ngOnInit() {
    return this.refresh();
  }

  async refresh() {
    await this.fetch(this.feedUrl);
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
    } catch (e) {
      this.errorMessage = e.message;
    }
    this.loading = false;
    this.changeRef.detectChanges();
  }
}
