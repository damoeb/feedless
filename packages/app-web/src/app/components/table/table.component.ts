import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  Input,
  input,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Subscription } from 'rxjs';
import { intersection, sortedUniqBy } from 'lodash-es';
import { NgStyle } from '@angular/common';

@Component({
  selector: 'app-table',
  templateUrl: './table.component.html',
  styleUrls: ['./table.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgStyle],
  standalone: true,
})
export class TableComponent<T> implements OnInit, OnDestroy {
  private readonly changeRef = inject(ChangeDetectorRef);

  private subscriptions: Subscription[] = [];

  @Input({ required: true })
  rows: T[];

  readonly columns = input<(keyof T)[]>();

  async ngOnInit(): Promise<void> {
    console.log(this.rows);
    this.subscriptions.push();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  getHeaders(): { name: keyof T }[] {
    if (this.rows) {
      const columns = this.getColumnIds();
      return sortedUniqBy(
        columns.map((name) => ({ name })),
        'name',
      );
    } else {
      return [];
    }
  }

  private getColumnIds() {
    if (this.rows) {
      const allColumns = Object.keys(this.rows[0]) as (keyof T)[];
      const columns = this.columns();
      if (columns) {
        return intersection(columns, allColumns);
      } else {
        return allColumns;
      }
    } else {
      return [];
    }
  }

  getRows() {
    return this.rows.map((row) =>
      this.getColumnIds().map((column) => ({ value: row[column] })),
    );
  }
}
