import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { GqlSearchMatch, GqlSearchQuery, GqlSearchQueryVariables, Search } from '../../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';

@Component({
  selector: 'app-search',
  templateUrl: './search.page.html',
  styleUrls: ['./search.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SearchPage implements OnInit {
  pagination: { __typename?: 'Pagination'; isEmpty: boolean; isFirst: boolean; isLast: boolean; page: number; totalPages: number };
  matches: Array<GqlSearchMatch>;
  loading = false;
  constructor(private readonly apollo: ApolloClient<any>,
              private readonly changeRef: ChangeDetectorRef) {}

  ngOnInit() {
    this.loading = true;
    this.apollo
      .query<GqlSearchQuery, GqlSearchQueryVariables>({
        query: Search,
      })
      .then(response => {
        let search = response.data.search;
        this.matches = search.matches
        this.pagination = search.pagination
      })
      .finally(() => {
        this.loading = false;
        this.changeRef.detectChanges();
      });
  }

  handleChange(event: any) {
    const query = event.target.value.toLowerCase();
    // this.results = this.data.filter(d => d.toLowerCase().indexOf(query) > -1);
  }

  toDate(createdAt: number): Date {
    return new Date(createdAt)
  }
}
