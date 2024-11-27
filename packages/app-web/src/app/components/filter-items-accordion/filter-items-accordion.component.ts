import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import {
  GqlCompositeFieldFilterParamsInput,
  GqlCompositeFilterParamsInput,
  GqlItemFilterParamsInput,
  GqlStringFilterOperator,
} from '../../../generated/graphql';
import { dateFormat } from '../../services/session.service';
import { debounce, interval, merge, ReplaySubject } from 'rxjs';
import { without } from 'lodash-es';
import { Repository, RepositoryFull } from '../../graphql/types';
import { ArrayElement, TypedFormGroup } from '../../types';
import { addIcons } from 'ionicons';
import { trashOutline, addOutline } from 'ionicons/icons';

export type FilterOperator = GqlStringFilterOperator;
export type FilterField = keyof GqlCompositeFieldFilterParamsInput;
export type FilterType = keyof GqlCompositeFilterParamsInput;

interface GeneralFilterData {
  type: FilterType;
  field: FilterField;
  operator: FilterOperator;
  value: string;
}

type GeneralFilterParams = ArrayElement<
  ArrayElement<Repository['plugins']>['params']['org_feedless_filter']
>;

@Component({
    selector: 'app-filter-feed-accordion',
    templateUrl: './filter-items-accordion.component.html',
    styleUrls: ['./filter-items-accordion.component.scss'],
    standalone: false
})
export class FilterItemsAccordionComponent implements OnInit {
  formFg = new FormGroup({
    applyFiltersLast: new FormControl<boolean>(false),
    filterExpression: new FormControl<string>(''),
  });
  filters: FormGroup<TypedFormGroup<GeneralFilterData>>[] = [];

  @Input({ required: true })
  filterPlugin: ArrayElement<
    RepositoryFull['plugins']
  >['params']['org_feedless_filter'];

  @Input({ required: true })
  labelPrefix: string;

  @Input()
  advanced: boolean;

  @Input()
  hideIfEmpty: boolean = false;

  @Input()
  disabled: boolean;

  @Output()
  filterChange: EventEmitter<GqlItemFilterParamsInput[]> = new EventEmitter<
    GqlItemFilterParamsInput[]
  >();

  filterChanges = new ReplaySubject<void>();

  protected readonly dateFormat = dateFormat;
  protected readonly GqlStringFilterOperator = GqlStringFilterOperator;
  protected FilterTypeInclude: FilterType = 'include';
  protected FilterTypeExclude: FilterType = 'exclude';
  protected FilterFieldLink: FilterField = 'link';
  protected FilterFieldTitle: FilterField = 'title';
  protected FilterFieldContent: FilterField = 'content';

  constructor() {
    addIcons({ trashOutline, addOutline });
  }

  addGeneralFilter(params: GeneralFilterParams = null, isNew = true) {
    if (isNew && this.filters.some((filter) => filter.invalid)) {
      return;
    }

    const filter = new FormGroup({
      type: new FormControl<FilterType>('exclude', [Validators.required]),
      field: new FormControl<FilterField>('title', [Validators.required]),
      operator: new FormControl<FilterOperator>(
        GqlStringFilterOperator.Contains,
        [Validators.required],
      ),
      value: new FormControl<string>('', [
        Validators.required,
        Validators.minLength(1),
      ]),
    });

    if (params?.composite) {
      const data = params.composite;
      const type = Object.keys(data).find(
        // @ts-ignore
        (field) => field != '__typename' && !!data[field],
      );
      // @ts-ignore
      const field = Object.keys(data[type]).find(
        // @ts-ignore
        (field) => field != '__typename' && !!data[type][field],
      );
      filter.patchValue({
        type: type as any,
        field: field as any,
        // @ts-ignore
        value: data[type][field].value,
        // @ts-ignore
        operator: data[type][field].operator,
      });
    }

    if (params?.expression) {
      this.formFg.controls.filterExpression.patchValue(params.expression);
    }

    if (isNew || params?.composite) {
      this.filters.push(filter);
      filter.statusChanges.subscribe((status) => {
        if (status === 'VALID') {
          this.filterChanges.next();
        }
      });
    }
  }

  removeFilter(index: number) {
    this.filters = without(this.filters, this.filters[index]);
    this.filterChanges.next();
  }

  async ngOnInit(): Promise<void> {
    if (this.filterPlugin) {
      this.filterPlugin.forEach((f) => this.addGeneralFilter(f, false));

      if (!this.hasCompositeFilters()) {
        this.addGeneralFilter(null, true);
      }
    } else {
      this.addGeneralFilter(null, true);
    }

    merge(
      this.formFg.controls.filterExpression.valueChanges,
      this.filterChanges,
    )
      .pipe(debounce(() => interval(100)))
      .subscribe(async () => {
        this.emitParams();
      });

    this.emitParams();
  }

  private getItemFilterParams(): GqlItemFilterParamsInput[] {
    const itemFilters = this.filters
      .filter((filterFg) => filterFg.valid)
      .map((filterFg) => filterFg.value)
      .map<GqlItemFilterParamsInput>((filter) => ({
        composite: {
          [filter.type]: {
            [filter.field]: {
              value: filter.value,
              operator: filter.operator,
            },
          },
        },
      }));

    if (this.hasFilterExpression()) {
      itemFilters.push({
        expression: this.formFg.value.filterExpression,
      });
      console.log(`expression: ${this.formFg.value.filterExpression}`);
    }
    return itemFilters;
  }

  hasFilters() {
    return this.hasCompositeFilters() || this.hasFilterExpression();
  }

  protected hasCompositeFilters() {
    return this.filters.filter((it) => it.valid).length > 0;
  }

  protected hasFilterExpression() {
    return this.formFg.value.filterExpression.trim().length > 0;
  }

  applyFiltersLast() {
    return this.formFg.value.applyFiltersLast;
  }

  countValidFilters() {
    return this.filters.filter((it) => it.valid).length;
  }

  private emitParams() {
    this.filterChange.emit(this.getItemFilterParams());
  }
}
