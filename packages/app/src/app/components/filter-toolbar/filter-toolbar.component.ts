import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { GqlContentSortTag } from '../../../generated/graphql';
import { FormControl, FormGroup } from '@angular/forms';
import { debounce, interval } from 'rxjs';
import { without } from 'lodash-es';
import { enumToKeyValue } from '../../pages/feeds/feeds.page';

export type FilterValues<T> = {
  [k in keyof T]: T[k][];
};

export interface FilterData<T> {
  sortBy: GqlContentSortTag;
  layout: Layout;
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

export enum Layout {
  grid = 'grid',
  list = 'list',
}

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

  sortByOptions = enumToKeyValue(GqlContentSortTag);

  sortByFormControl: FormControl<GqlContentSortTag | null>;
  layoutFormControl: FormControl<Layout | null>;

  showFilters = false;
  filterFormGroup: FormGroup;

  constructor() {}

  ngOnInit() {
    this.sortByFormControl = new FormControl<GqlContentSortTag>(
      GqlContentSortTag.Newest
    );
    this.layoutFormControl = new FormControl<Layout>(Layout.list);

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
      this.emit();
    });

    this.emit();
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

  changeLayout() {
    if (this.layoutFormControl.value === Layout.list) {
      this.layoutFormControl.setValue(Layout.grid);
    } else {
      this.layoutFormControl.setValue(Layout.list);
    }
  }

  private emit() {
    const filterData: FilterData<T> = {
      sortBy: this.sortByFormControl.value,
      layout: this.layoutFormControl.value,
      filters: this.filterFormGroup.value,
    };
    this.appFilterChange.emit(filterData);
  }
}
