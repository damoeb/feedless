import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  inject,
} from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

import { Feed, FeedItem } from '../../data/feeds';

@Component({
  selector: 'app-article-view',
  templateUrl: './article-view.component.html',
  styleUrls: ['./article-view.component.scss'],
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ArticleViewComponent implements OnChanges {
  private readonly sanitizer = inject(DomSanitizer);

  @Input() item: FeedItem | null = null;
  @Input() feeds: Feed[] = [];

  @Output() readonly toggleStar = new EventEmitter<string>();

  contentHtml: SafeHtml | null = null;

  ngOnChanges(): void {
    this.contentHtml =
      this.item?.content != null
        ? this.sanitizer.bypassSecurityTrustHtml(this.item.content)
        : null;
  }

  feedTitle(): string {
    if (!this.item) {
      return '';
    }
    return this.feeds.find((f) => f.id === this.item!.feedId)?.title ?? '';
  }

  formatFullDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: 'numeric',
      minute: '2-digit',
    });
  }

  onStarClick(): void {
    if (this.item) {
      this.toggleStar.emit(this.item.id);
    }
  }
}
