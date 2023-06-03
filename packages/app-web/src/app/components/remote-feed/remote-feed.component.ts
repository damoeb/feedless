import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnInit,
} from '@angular/core';
import { FeedService } from '../../services/feed.service';
import { FieldWrapper, Scalars } from '../../../generated/graphql';
import { WizardHandler } from '../wizard/wizard-handler';
import { RemoteFeedItem } from '../../graphql/types';
import { WizardContext } from '../wizard/wizard/wizard.component';

@Component({
  selector: 'app-remote-feed',
  templateUrl: './remote-feed.component.html',
  styleUrls: ['./remote-feed.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RemoteFeedComponent implements OnInit {
  @Input()
  handler: WizardHandler;
  @Input()
  title = 'Feed Preview';
  @Input()
  showTitle = true;

  feedUrl: string;
  filter = '';
  loading: boolean;
  feedItems: Array<RemoteFeedItem>;
  errorMessage: string;
  filterChanged: boolean;

  constructor(
    private readonly feedService: FeedService,
    private readonly changeRef: ChangeDetectorRef
  ) {}

  async ngOnInit(): Promise<void> {
    await this.handleContext(this.handler.getContext());
    this.handler.onContextChange().subscribe(async (context) => {
      if (context) {
        await this.handleContext(context);
      }
    });
  }

  toDate(publishedAt: FieldWrapper<Scalars['Long']>): Date {
    return new Date(publishedAt);
  }

  async refresh() {
    await this.fetch(this.feedUrl, this.filter);
  }

  private async handleContext(context: Partial<WizardContext>) {
    let detect = false;
    if (context.importer?.filter && this.filter !== context.importer.filter) {
      this.filter = context.importer.filter;
      this.filterChanged = true;
      detect = true;
    }
    if (context.feedUrl && this.feedUrl !== context.feedUrl) {
      this.feedUrl = context.feedUrl;
      await this.fetch(
        this.feedUrl,
        this.handler.getContext().importer?.filter
      );
      detect = true;
    }
    if (detect) {
      this.changeRef.detectChanges();
    }
  }

  private async fetch(nativeFeedUrl: string, filter: string): Promise<void> {
    this.loading = true;
    this.feedItems = [];
    this.changeRef.detectChanges();
    try {
      this.feedItems = await this.feedService.remoteFeedContent({
        nativeFeedUrl,
        applyFilter: filter ? { filter } : undefined,
      });
    } catch (e) {
      this.errorMessage = e.message;
    }
    this.loading = false;
    this.filterChanged = false;
    this.changeRef.detectChanges();
  }
}
