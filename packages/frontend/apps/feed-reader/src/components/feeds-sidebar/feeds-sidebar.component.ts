import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  Output,
} from '@angular/core';

import { Feed } from '../../data/feeds';

@Component({
  selector: 'app-feeds-sidebar',
  templateUrl: './feeds-sidebar.component.html',
  styleUrls: ['./feeds-sidebar.component.scss'],
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeedsSidebarComponent {
  /** Matches React: only these folders start expanded; others start collapsed. */
  expandedFolders: Record<string, boolean> = {
    Tech: true,
    Blogs: true,
    Design: true,
  };

  @Input() feeds: Feed[] = [];
  @Input() selectedFeedId: string | null = null;
  @Input() selectedView = 'all';

  @Output() readonly selectFeed = new EventEmitter<string>();
  @Output() readonly selectView = new EventEmitter<string>();

  readonly starredCount = 3;

  totalUnread(): number {
    return this.feeds.reduce((sum, f) => sum + (f.unreadCount ?? 0), 0);
  }

  folderEntries(): [string, Feed[]][] {
    const acc: Record<string, Feed[]> = {};
    for (const feed of this.feeds) {
      const folder = feed.folder || 'Uncategorized';
      if (!acc[folder]) {
        acc[folder] = [];
      }
      acc[folder].push(feed);
    }
    return Object.entries(acc);
  }

  folderUnread(folderFeeds: Feed[]): number {
    return folderFeeds.reduce((s, f) => s + (f.unreadCount ?? 0), 0);
  }

  isFolderExpanded(folder: string): boolean {
    return this.expandedFolders[folder] === true;
  }

  toggleFolder(folder: string): void {
    this.expandedFolders = {
      ...this.expandedFolders,
      [folder]: !this.isFolderExpanded(folder),
    };
  }

  isViewActive(view: string): boolean {
    return this.selectedView === view && !this.selectedFeedId;
  }

  onSelectAll(): void {
    this.selectView.emit('all');
    this.selectFeed.emit('');
  }

  onSelectStarred(): void {
    this.selectView.emit('starred');
    this.selectFeed.emit('');
  }

  onSelectFeedRow(feed: Feed): void {
    this.selectFeed.emit(feed.id);
    this.selectView.emit('feed');
  }
}
