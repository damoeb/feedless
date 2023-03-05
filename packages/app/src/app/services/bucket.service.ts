import { Injectable } from '@angular/core';
import {
  BucketById,
  CreateBucket,
  DeleteBucket,
  GqlBucket,
  GqlBucketByIdQuery,
  GqlBucketByIdQueryVariables,
  GqlBucketCreateInput,
  GqlBucketsPagedInput,
  GqlBucketUpdateInput,
  GqlCreateBucketMutation,
  GqlCreateBucketMutationVariables,
  GqlDeleteBucketMutation,
  GqlDeleteBucketMutationVariables,
  GqlGenericFeed,
  GqlSearchBucketsQuery,
  GqlSearchBucketsQueryVariables,
  GqlUpdateBucketMutation,
  GqlUpdateBucketMutationVariables,
  Maybe,
  SearchBuckets,
  UpdateBucket,
} from '../../generated/graphql';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import { BasicImporter } from './importer.service';
import { BasicNativeFeed } from './feed.service';
import { Pagination } from './pagination.service';
import { AlertController } from '@ionic/angular';

export type BasicBucket = Pick<
  GqlBucket,
  | 'id'
  | 'title'
  | 'description'
  | 'imageUrl'
  | 'streamId'
  | 'websiteUrl'
  | 'lastUpdatedAt'
  | 'createdAt'
  | 'tags'
>;

export type Bucket = BasicBucket & {
  importers?: Maybe<
    Array<
      BasicImporter & {
        nativeFeed: BasicNativeFeed & {
          genericFeed?: Maybe<Pick<GqlGenericFeed, 'id'>>;
        };
      }
    >
  >;
};

export type BucketMetadata = Pick<
  GqlBucket,
  'title' | 'description' | 'imageUrl' | 'websiteUrl' | 'tags'
>;

@Injectable({
  providedIn: 'root',
})
export class BucketService {
  constructor(
    private readonly apollo: ApolloClient<any>,
    private readonly alertController: AlertController
  ) {}

  getBucketById(
    id: string,
    fetchPolicy: FetchPolicy = 'cache-first'
  ): Promise<Bucket> {
    return this.apollo
      .query<GqlBucketByIdQuery, GqlBucketByIdQueryVariables>({
        query: BucketById,
        fetchPolicy,
        variables: {
          data: {
            where: {
              id,
            },
          },
        },
      })
      .then((response) => response.data.bucket);
  }

  search(
    data: GqlBucketsPagedInput
  ): Promise<{ buckets: Array<BasicBucket>; pagination: Pagination }> {
    return this.apollo
      .query<GqlSearchBucketsQuery, GqlSearchBucketsQueryVariables>({
        query: SearchBuckets,
        variables: {
          data,
        },
      })
      .then((response) => response.data.buckets);
  }

  async deleteBucket(id: string): Promise<void> {
    await this.apollo.mutate<
      GqlDeleteBucketMutation,
      GqlDeleteBucketMutationVariables
    >({
      mutation: DeleteBucket,
      variables: {
        data: {
          where: {
            id,
          },
        },
      },
    });
  }

  createBucket(data: GqlBucketCreateInput): Promise<Pick<GqlBucket, 'id'>> {
    return this.apollo
      .mutate<GqlCreateBucketMutation, GqlCreateBucketMutationVariables>({
        mutation: CreateBucket,
        variables: {
          data,
        },
      })
      .then((response) => response.data.createBucket);
  }

  updateBucket(data: GqlBucketUpdateInput): Promise<Pick<GqlBucket, 'id'>> {
    return this.apollo
      .mutate<GqlUpdateBucketMutation, GqlUpdateBucketMutationVariables>({
        mutation: UpdateBucket,
        variables: {
          data,
        },
      })
      .then((response) => response.data.updateBucket);
  }

  async showBucketAlert(
    title: string,
    bucket?: BucketMetadata
  ): Promise<BucketMetadata> {
    const alert = await this.alertController.create({
      header: title,
      buttons: [
        {
          text: 'Cancel',
          role: 'cancel',
        },
        {
          text: 'Save',
          role: 'confirm',
          handler: () => {},
        },
      ],
      inputs: [
        {
          name: 'title',
          placeholder: 'Title',
          attributes: {
            required: true,
          },
          value: bucket?.title,
        },
        {
          name: 'websiteUrl',
          placeholder: 'Website Url',
          value: bucket?.websiteUrl,
        },
        {
          name: 'imageUrl',
          placeholder: 'Image Url',
          value: bucket?.imageUrl,
        },
        {
          name: 'tags',
          placeholder: 'Tags',
          value: bucket?.tags,
        },
        {
          name: 'description',
          placeholder: 'Description',
          type: 'textarea',
          value: bucket?.description,
        },
      ],
    });

    await alert.present();
    const data = await alert.onDidDismiss();
    return data.data.values as BucketMetadata;
  }
}
