import { Injectable } from '@angular/core';
import { Apollo } from 'apollo-angular';
import { GqlArticlesByStreamIdGQL } from '../../generated/graphql';

@Injectable({
  providedIn: 'root',
})
export class StreamService {
  constructor(
    private readonly apollo: Apollo,
    private readonly articlesByStreamIdGQL: GqlArticlesByStreamIdGQL
  ) {}

  getArticles(streamId: string, skip: number = 0, take: number = 10) {
    console.log('getArticles', streamId, skip, take);
    return this.articlesByStreamIdGQL.fetch({
      streamId,
      take,
      skip,
    });
  }
}
