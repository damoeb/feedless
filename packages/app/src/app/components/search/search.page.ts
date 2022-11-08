import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ApolloClient } from '@apollo/client/core';
import { ModalController } from '@ionic/angular';
import { BucketCreatePage } from '../bucket-create/bucket-create.page';
import { GqlSearchBucketMatch, GqlSearchBucketsQuery, GqlSearchBucketsQueryVariables, SearchBuckets } from '../../../generated/graphql';

@Component({
  selector: 'app-search',
  templateUrl: './search.page.html',
  styleUrls: ['./search.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SearchPage implements OnInit {
  pagination: { __typename?: 'Pagination'; isEmpty: boolean; isFirst: boolean; isLast: boolean; page: number; totalPages: number };
  matches: Array<GqlSearchBucketMatch>;
  loading = false;
  query: string = '';
  constructor(private readonly apollo: ApolloClient<any>,
              private readonly modalController: ModalController,
              private readonly changeRef: ChangeDetectorRef) {}

  ngOnInit() {
    this.handleChange();
  }

  handleChange() {
    this.loading = true;
    this.apollo
      .query<GqlSearchBucketsQuery, GqlSearchBucketsQueryVariables>({
        query: SearchBuckets,
        variables: {
          query: this.query,
          corrId: '1234'
        }
      })
      .then(response => {
        let search = response.data.searchBucket;
        this.matches = search.matches
        this.pagination = search.pagination
      })
      .finally(() => {
        this.loading = false;
        this.changeRef.detectChanges();
      });

  }

  toDate(createdAt: number): Date {
    return new Date(createdAt)
  }

  async showCreateBucketModal() {
    const modal = await this.modalController.create({
      component: BucketCreatePage
    });
    await modal.present()
  }
}
