import { Injectable } from '@angular/core';
import { Observable, ReplaySubject } from 'rxjs';
import { ArticleWithContext } from '../graphql/types';

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
