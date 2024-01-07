import { Injectable } from '@angular/core';
import {
  CreateNativeFeeds,
  DeleteNativeFeed,
  GqlCreateNativeFeedsInput,
  GqlCreateNativeFeedsMutation,
  GqlCreateNativeFeedsMutationVariables,
  GqlDeleteNativeFeedMutation,
  GqlDeleteNativeFeedMutationVariables,
  GqlMarkupTransformer,
  GqlNativeFeed,
  GqlNativeFeedByIdQuery,
  GqlNativeFeedByIdQueryVariables,
  GqlNativeFeedsInput,
  GqlNativeFeedUpdateInput,
  GqlNativeFeedWhereInput,
  GqlRemoteNativeFeedInput,
  GqlRemoteNativeFeedQuery,
  GqlRemoteNativeFeedQueryVariables,
  GqlScrapedFeeds,
  GqlScrapeQuery,
  GqlScrapeQueryVariables,
  GqlSearchNativeFeedsQuery,
  GqlSearchNativeFeedsQueryVariables,
  GqlUpdateNativeFeedMutation,
  GqlUpdateNativeFeedMutationVariables,
  NativeFeedById,
  RemoteNativeFeed,
  Scrape,
  SearchNativeFeeds,
  UpdateNativeFeed,
} from '../../generated/graphql';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import {
  FeedDiscoveryResult,
  FetchOptions,
  NativeFeed,
  NativeFeeds,
  RemoteFeed,
  RemoteFeedItem,
} from '../graphql/types';

@Injectable({
  providedIn: 'root',
})
export class FeedService {
  constructor(private readonly apollo: ApolloClient<any>) {}

  getNativeFeed(
    data: GqlNativeFeedWhereInput,
    fetchPolicy: FetchPolicy = 'cache-first',
  ): Promise<NativeFeed> {
    return this.apollo
      .query<GqlNativeFeedByIdQuery, GqlNativeFeedByIdQueryVariables>({
        query: NativeFeedById,
        fetchPolicy,
        variables: {
          data,
        },
      })
      .then((response) => response.data.nativeFeed as NativeFeed);
  }

  discoverFeeds(fetchOptions: FetchOptions): Promise<FeedDiscoveryResult> {
    return this.apollo
      .query<GqlScrapeQuery, GqlScrapeQueryVariables>({
        query: Scrape,
        variables: {
          data: {
            page: {
              url: fetchOptions.websiteUrl,
              prerender: fetchOptions.prerender
                ? {
                    waitUntil: fetchOptions.prerenderWaitUntil,
                  }
                : null,
            },
            emit: [
              {
                selectorBased: {
                  xpath: {
                    value: '/',
                  },
                  expose: {
                    transformers: [
                      {
                        internal: {
                          transformer: GqlMarkupTransformer.Feeds,
                        },
                      },
                    ],
                  },
                },
              },
            ],
          },
        },
      })
      .then((response) => {
        const scrape = response.data.scrape;
        const element = scrape.elements[0];
        const feeds = JSON.parse(
          element.selector.fields.find(
            (field) => field.name === GqlMarkupTransformer.Feeds,
          ).value.one.data,
        ) as GqlScrapedFeeds;
        const markup = element.selector.html.data;
        return {
          fetchOptions,
          document: {
            url: scrape.url,
            htmlBody: markup,
            mimeType: scrape.debug.contentType,
          },
          genericFeeds: feeds.genericFeeds,
          nativeFeeds: feeds.nativeFeeds,
          websiteUrl: scrape.url,
          errorMessage: scrape.errorMessage,
          failed: scrape.failed,
        } as FeedDiscoveryResult;
      });
  }

  searchNativeFeeds(
    data: GqlNativeFeedsInput,
    fetchPolicy: FetchPolicy,
  ): Promise<NativeFeeds> {
    return this.apollo
      .query<GqlSearchNativeFeedsQuery, GqlSearchNativeFeedsQueryVariables>({
        query: SearchNativeFeeds,
        variables: {
          data,
        },
        fetchPolicy,
      })
      .then((response) => response.data.nativeFeeds);
  }

  async deleteNativeFeed(id: string): Promise<void> {
    await this.apollo.mutate<
      GqlDeleteNativeFeedMutation,
      GqlDeleteNativeFeedMutationVariables
    >({
      mutation: DeleteNativeFeed,
      variables: {
        data: {
          nativeFeed: { id },
        },
      },
    });
  }

  async createNativeFeeds(
    data: GqlCreateNativeFeedsInput,
  ): Promise<Array<Pick<GqlNativeFeed, 'id'>>> {
    return this.apollo
      .mutate<
        GqlCreateNativeFeedsMutation,
        GqlCreateNativeFeedsMutationVariables
      >({
        mutation: CreateNativeFeeds,
        variables: {
          data,
        },
      })
      .then((response) => response.data.createNativeFeeds);
  }

  async remoteFeedContent(
    data: GqlRemoteNativeFeedInput,
  ): Promise<Array<RemoteFeedItem>> {
    return this.remoteFeed(data).then((response) => response.items);
  }

  async remoteFeed(data: GqlRemoteNativeFeedInput): Promise<RemoteFeed> {
    return this.apollo
      .query<GqlRemoteNativeFeedQuery, GqlRemoteNativeFeedQueryVariables>({
        query: RemoteNativeFeed,
        variables: {
          data,
        },
      })
      .then((response) => response.data.remoteNativeFeed);
  }

  async updateNativeFeed(data: GqlNativeFeedUpdateInput): Promise<any> {
    return this.apollo
      .mutate<
        GqlUpdateNativeFeedMutation,
        GqlUpdateNativeFeedMutationVariables
      >({
        mutation: UpdateNativeFeed,
        variables: {
          data,
        },
      })
      .then((response) => response.data.updateNativeFeed);
  }
}
