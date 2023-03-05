import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import {
  GqlArticleType,
  GqlContentCategoryTag,
  GqlContentSortTag,
  GqlContentTypeTag,
  GqlArticleReleaseStatus,
} from '../../../generated/graphql';
import { FormControl, FormGroup } from '@angular/forms';
import { interval, debounce } from 'rxjs';
import { without } from 'lodash';

export interface FilterQuery {
  query: string;
  sortBy: GqlContentSortTag;
  contentCategory: GqlContentCategoryTag[];
  contentType: GqlContentTypeTag[];
  articleType: GqlArticleType[];
  releaseStatus: GqlArticleReleaseStatus[];
}

export interface Filter<T> {
  name: string;
  control: FormControl<T[]>;
  options: T[];
}

export interface Filters {
  [k: string]: Filter<any>;
}

type Layout = 'grid' | 'list';

@Component({
  selector: 'app-filter-toolbar',
  templateUrl: './filter-toolbar.component.html',
  styleUrls: ['./filter-toolbar.component.scss'],
})
export class FilterToolbarComponent<T> implements OnInit {
  @Input()
  filters: Filters;

  @Output()
  appFilterChange: EventEmitter<FilterQuery> = new EventEmitter<FilterQuery>();

  sortByOptions = Object.values(GqlContentSortTag);
  showFilters = false;
  filterFormGroup: FormGroup;
  layoutOptions: Layout[] = ['list', 'grid'];

  sortByFormControl: FormControl<GqlContentSortTag | null>;
  layoutFormControl: FormControl<Layout | null>;

  constructor() {}

  ngOnInit() {
    this.sortByFormControl = new FormControl<GqlContentSortTag>(
      GqlContentSortTag.Newest
    );
    this.layoutFormControl = new FormControl<Layout>('list');
    this.filterFormGroup = new FormGroup({
      ...Object.keys(this.filters).reduce((map, key) => {
        map[key] = this.filters[key].control;
        return map;
      }, {}),
    });

    const formGroup = new FormGroup(
      {
        sortBy: this.sortByFormControl,
        layout: this.layoutFormControl,
        filters: this.filterFormGroup,
      },
      { updateOn: 'change' }
    );

    formGroup.valueChanges.pipe(debounce(() => interval(500))).subscribe(() => {
      console.log('this.formGroup.value', formGroup.pristine);
      this.appFilterChange.emit(formGroup.value as FilterQuery);
    });

    this.appFilterChange.emit(formGroup.value as FilterQuery);
  }

  formControls(): Filter<unknown>[] {
    return Object.values(this.filters);
  }

  applyOption(control: FormControl<any[]>, tag: string) {
    const value = control.value as any[];
    if (value.indexOf(tag) > -1) {
      control.setValue(without(value, tag));
    } else {
      control.setValue([...value, tag]);
    }
  }
}
