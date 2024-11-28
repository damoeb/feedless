import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
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
  private subscriptions: Subscription[] = [];

  @Input({ required: true })
  rows: T[];

  @Input()
  columns: (keyof T)[];

  constructor(private readonly changeRef: ChangeDetectorRef) {}

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
      if (this.columns) {
        return intersection(this.columns, allColumns);
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
