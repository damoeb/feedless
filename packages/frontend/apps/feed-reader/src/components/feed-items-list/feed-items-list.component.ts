import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  Output,
} from '@angular/core';

import { Feed, FeedItem } from '../../data/feeds';

@Component({
  selector: 'app-feed-items-list',
  templateUrl: './feed-items-list.component.html',
  styleUrls: ['./feed-items-list.component.scss'],
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeedItemsListComponent {
  @Input() items: FeedItem[] = [];
  @Input() feeds: Feed[] = [];
  @Input() selectedItemId: string | null = null;
  @Input() viewTitle = '';

  @Output() readonly selectItem = new EventEmitter<string>();
  @Output() readonly toggleStar = new EventEmitter<string>();

  formatDate(dateStr: string): string {
    const d = new Date(dateStr);
    const now = new Date();
    const diff = now.getTime() - d.getTime();
    const hours = Math.floor(diff / 3600000);
    if (hours < 1) {
      return 'just now';
    }
    if (hours < 24) {
      return `${hours}h ago`;
    }
    const days = Math.floor(hours / 24);
    if (days < 7) {
      return `${days}d ago`;
    }
    return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  }

  feedTitle(feedId: string): string {
    return this.feeds.find((f) => f.id === feedId)?.title ?? '';
  }

  onRowClick(itemId: string): void {
    this.selectItem.emit(itemId);
  }

  onStarClick(event: Event, itemId: string): void {
    event.stopPropagation();
    this.toggleStar.emit(itemId);
  }
}
