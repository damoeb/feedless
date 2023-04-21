import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { GqlContentSortTag } from '../../../generated/graphql';
import { FormControl, FormGroup } from '@angular/forms';
import { debounce, interval, Subscription } from 'rxjs';
import { without } from 'lodash-es';

export type FilterValues<T> = {
  [k in keyof T]: T[k][];
};

export interface FilterData<T> {
  sortBy: GqlContentSortTag;
  filters: FilterValues<T>;
}

export interface Filter<T> {
  name: string;
  control: FormControl<T[]>;
  options: FilterOption[];
}

export type Filters<T> = {
  [k in keyof T]: Filter<T[k]>;
};

export type FilterOption = {
  key: string;
  value: string;
};
// export type FilterOptions<T> = {
//   [k in keyof T]: T[k];
// };

@Component({
  selector: 'app-filter-toolbar',
  templateUrl: './filter-toolbar.component.html',
  styleUrls: ['./filter-toolbar.component.scss'],
})
export class FilterToolbarComponent<T> implements OnInit {
  @Input()
  filters: Filters<T>;

  @Output()
  appFilterChange: EventEmitter<FilterData<T>> = new EventEmitter<
    FilterData<T>
  >();

  sortByFormControl: FormControl<GqlContentSortTag | null>;

  showFilters = false;
  filterFormGroup: FormGroup;

  private subscriptions: Subscription[] = [];

  constructor() {}

  ngOnInit() {
    this.sortByFormControl = new FormControl<GqlContentSortTag>(
      GqlContentSortTag.Newest
    );

    this.filterFormGroup = new FormGroup({
      ...Object.keys(this.filters).reduce((map, key) => {
        map[key] = this.filters[key].control;
        return map;
      }, {}),
    });

    const formGroup = new FormGroup(
      {
        sortBy: this.sortByFormControl,
        filters: this.filterFormGroup,
      },
      { updateOn: 'change' }
    );

    this.subscriptions.push(
      formGroup.valueChanges
        .pipe(debounce(() => interval(500)))
        .subscribe(() => {
          this.emit();
        })
    );

    this.emit();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
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

  toggleSortBy() {
    if (this.sortByFormControl.value === GqlContentSortTag.Newest) {
      this.sortByFormControl.setValue(GqlContentSortTag.Oldest);
    } else {
      this.sortByFormControl.setValue(GqlContentSortTag.Newest);
    }
  }

  private emit() {
    const filterData: FilterData<T> = {
      sortBy: this.sortByFormControl.value,
      filters: this.filterFormGroup.value,
    };
    this.appFilterChange.emit(filterData);
  }
}
