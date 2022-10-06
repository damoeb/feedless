import { Component, OnInit } from '@angular/core';
import {
  GqlSearchQuery,
  GqlSearchQueryVariables,
  Search,
} from '../../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';

@Component({
  selector: 'app-search',
  templateUrl: './search.page.html',
  styleUrls: ['./search.page.scss'],
})
export class SearchPage implements OnInit {
  constructor(private readonly apollo: ApolloClient<any>) {}

  ngOnInit() {
    this.apollo
      .query<GqlSearchQuery, GqlSearchQueryVariables>({
        query: Search,
      })
      .then(console.log);
  }

  handleChange(event: any) {
    const query = event.target.value.toLowerCase();
    // this.results = this.data.filter(d => d.toLowerCase().indexOf(query) > -1);
  }
}
