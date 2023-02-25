import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import {
  GqlContentCategoryTag,
  GqlContentScopeTag,
  GqlContentSortTag,
} from '../../../generated/graphql';

export interface FilterQuery {
  query: string;
}

@Component({
  selector: 'app-filter-toolbar',
  templateUrl: './filter-toolbar.component.html',
  styleUrls: ['./filter-toolbar.component.scss'],
})
export class FilterToolbarComponent implements OnInit {
  @Input()
  placeholder: string;

  @Output()
  appChange: EventEmitter<FilterQuery> = new EventEmitter<FilterQuery>();

  contentTags: string[];
  sortTags: string[];
  query: string;

  constructor() {}

  ngOnInit() {
    this.contentTags = [
      ...Object.values(GqlContentCategoryTag),
      ...Object.values(GqlContentScopeTag),
    ];
    this.sortTags = [...Object.values(GqlContentSortTag)];
  }
}
