export interface Feed {
  id: string;
  title: string;
  folder?: string;
  unreadCount?: number;
}

export interface FeedItem {
  id: string;
  feedId: string;
  title: string;
  author: string;
  date: string;
  url: string;
  content: string;
  summary: string;
  read: boolean;
  starred: boolean;
}
