import { Input } from '@angular/core';
import {
  ActionSheetController,
  InfiniteScrollCustomEvent,
} from '@ionic/angular';
import { without } from 'lodash';
import { Pagination } from '../services/pagination.service';
import { ActionSheetButton } from '@ionic/core/dist/types/components/action-sheet/action-sheet-interface';

export abstract class FilteredList<T, F> {
  @Input()
  streamId: string;
  pagination: Pagination;
  currentPage = 0;
  entities: Array<T> = [];

  checkedEntities: Array<T> = [];
  private filterData: F;

  constructor(
    protected entityName: string,
    protected readonly actionSheetCtrl: ActionSheetController
  ) {}

  async showActions() {
    const actionSheet = await this.actionSheetCtrl.create({
      header: `Actions for ${this.checkedEntities.length} ${this.entityName}s`,
      buttons: this.getBulkActionButtons(),
    });

    await actionSheet.present();

    const result = await actionSheet.onDidDismiss();
  }

  toggleCheckAll(event: any) {
    if (event.detail.checked) {
      this.checkedEntities = [...this.entities];
    } else {
      this.checkedEntities = [];
    }
  }

  onCheckChange(event: any, entity: T) {
    if (event.detail.checked) {
      this.checkedEntities.push(entity);
    } else {
      this.checkedEntities = without(this.checkedEntities, entity);
    }
  }

  isChecked(entity: T): boolean {
    return this.checkedEntities.indexOf(entity) > -1;
  }

  async firstPage(filterData: F) {
    this.filterData = filterData;
    this.entities = [];
    this.currentPage = 0;
    await this.triggerFetch();
    this.onDidChange();
  }

  async nextPage(event: InfiniteScrollCustomEvent) {
    if (!this.pagination.isLast) {
      this.currentPage++;
      await this.triggerFetch();
      await event.target.complete();
      this.onDidChange();
    }
  }

  onDidChange() {}

  private async triggerFetch() {
    const [entities, pagination] = await this.fetch(this.filterData);
    this.entities.push(...entities);
    this.pagination = pagination;
  }

  abstract getBulkActionButtons(): ActionSheetButton[];

  abstract fetch(filterData: F): Promise<[T[], Pagination]>;
}
