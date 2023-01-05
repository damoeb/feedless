import { Injectable } from '@angular/core';
import { ArticleWithContext } from './article.service';
import { Observable, ReplaySubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class PlayerService {
  private playlist: ArticleWithContext[] = [];
  private playlistSj = new ReplaySubject<ArticleWithContext[]>();

  constructor() {}

  pushFirst(article: ArticleWithContext) {
    this.playlist.unshift(article);
    this.playlistSj.next(this.playlist);
  }

  watchPlaylist(): Observable<ArticleWithContext[]> {
    return this.playlistSj.asObservable();
  }
}
