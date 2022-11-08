import { Injectable } from '@angular/core';
import {
  CreateNativeFeed,
  DeleteGenericFeed,
  DeleteImporter,
  DeleteNativeFeed,
  DiscoverFeeds,
  GenericFeedById,
  GqlCreateNativeFeedMutation,
  GqlCreateNativeFeedMutationVariables,
  GqlDeleteGenericFeedMutation,
  GqlDeleteGenericFeedMutationVariables,
  GqlDeleteImporterMutation,
  GqlDeleteImporterMutationVariables,
  GqlDeleteNativeFeedMutation,
  GqlDeleteNativeFeedMutationVariables,
  GqlDiscoverFeedsQuery,
  GqlDiscoverFeedsQueryVariables,
  GqlFeedDiscoveryResponse,
  GqlGenericFeed,
  GqlGenericFeedByIdQuery,
  GqlGenericFeedByIdQueryVariables,
  GqlGenericFeedRule,
  GqlNativeFeed,
  GqlNativeFeedByIdQuery,
  GqlNativeFeedByIdQueryVariables, GqlNativeFeedCreateInput,
  GqlNativeFeedReference,
  GqlPagination,
  GqlSearchFeedMatch,
  GqlSearchFeedsQuery,
  GqlSearchFeedsQueryVariables,
  Maybe,
  NativeFeedById,
  SearchFeeds
} from '../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';
import { SettingsService } from './settings.service';

export type NativeFeed = GqlNativeFeed
export type GenericFeedRef = Pick<GqlGenericFeed, 'id' | 'feedRule' | 'nativeFeedId'>
export type FeedDiscovery = (
  Pick<GqlFeedDiscoveryResponse, 'mimeType' | 'failed' | 'errorMessage'>
  & { genericFeedRules?: Maybe<Array<Pick<GqlGenericFeedRule, 'feedUrl' | 'score' | 'linkXPath' | 'extendContext' | 'dateXPath' | 'count' | 'contextXPath'>>>, nativeFeeds?: Maybe<Array<Pick<GqlNativeFeedReference, 'url' | 'type' | 'description' | 'title'>>> }
  )
export type SearchResponse = { matches?: Maybe<Array<Pick<GqlSearchFeedMatch, 'id' | 'title' | 'subtitle' | 'url' | 'createdAt'>>>, pagination: Pick<GqlPagination, 'isEmpty' | 'isFirst' | 'isLast' | 'page' | 'totalPages'> }


@Injectable({
  providedIn: 'root'
})
export class FeedService {

  constructor(private readonly apollo: ApolloClient<any>,
              private readonly settings: SettingsService) { }

  getNativeFeedById(id: string): Promise<NativeFeed> {
    return this.apollo
      .query<GqlNativeFeedByIdQuery, GqlNativeFeedByIdQueryVariables>({
        query: NativeFeedById,
        variables: {
          id
        }
      })
      .then(response => {
        return response.data.nativeFeed as NativeFeed;
      });
  }

  discoverFeeds(url: string): Promise<FeedDiscovery> {
    return this.apollo
      .query<GqlDiscoverFeedsQuery, GqlDiscoverFeedsQueryVariables>({
        query: DiscoverFeeds,
        variables: {
          url,
          corrId: this.settings.getCorrId(),
          prerender: false
        }
      })
      .then(response => {
        return response.data.discoverFeeds;
      });

  }

  getGenericFeedById(id: string): Promise<GenericFeedRef> {
    return this.apollo
      .query<GqlGenericFeedByIdQuery, GqlGenericFeedByIdQueryVariables>({
        query: GenericFeedById,
        variables: {
          id
        }
      })
      .then(response => {
        return response.data.genericFeed;
      });

  }

  searchFeeds(query: string): Promise<SearchResponse> {
    return this.apollo
      .query<GqlSearchFeedsQuery, GqlSearchFeedsQueryVariables>({
        query: SearchFeeds,
        variables: {
          corrId: this.settings.getCorrId(),
          query
        }
      })
      .then(response => {
        return response.data.searchFeed;
      })
  }

  async deleteNativeFeed(id: string): Promise<void> {
    await this.apollo
      .mutate<GqlDeleteNativeFeedMutation, GqlDeleteNativeFeedMutationVariables>({
        mutation: DeleteNativeFeed,
        variables: {
          id
        }
      })
  }

  async deleteGenericFeed(id: string): Promise<void> {
    await this.apollo
      .mutate<GqlDeleteGenericFeedMutation, GqlDeleteGenericFeedMutationVariables>({
        mutation: DeleteGenericFeed,
        variables: {
          id
        }
      })
  }

  async createNativeFeed(data: GqlNativeFeedCreateInput): Promise<Pick<GqlNativeFeed, "id">> {
    return this.apollo
      .mutate<GqlCreateNativeFeedMutation, GqlCreateNativeFeedMutationVariables>({
        mutation: CreateNativeFeed,
        variables: {
          data
        }
      }).then(response =>
        response.data.createNativeFeed.feed
      )
  }
}
