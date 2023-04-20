import { Component, Input } from '@angular/core';
import { ActionSheetController } from '@ionic/angular';
import { without } from 'lodash-es';
import { Pagination } from '../services/pagination.service';
import { ActionSheetButton } from '@ionic/core/dist/types/components/action-sheet/action-sheet-interface';

@Component({
  template: '',
})
export abstract class FilteredList<T, F> {
  @Input()
  streamId: string;
  pagination: Pagination;
  entities: Array<T> = [];
  checkedEntities: Array<T> = [];

  filterData: F;
  private currentPage = 0;

  protected constructor(
    protected readonly actionSheetCtrl: ActionSheetController
  ) {}

  // async showActions() {
  //   const actionSheet = await this.actionSheetCtrl.create({
  //     header: `Actions for ${this.checkedEntities.length} Itemss`,
  //     buttons: this.getBulkActionButtons(),
  //   });
  //
  //   await actionSheet.present();
  //   await actionSheet.onDidDismiss();
  // }

  // toggleCheckAll(event: any) {
  //   if (event.detail.checked) {
  //     this.checkedEntities = [...this.entities];
  //   } else {
  //     this.checkedEntities = [];
  //   }
  // }
  //
  // onCheckChange(event: any, entity: T) {
  //   if (event.detail.checked) {
  //     this.checkedEntities.push(entity);
  //   } else {
  //     this.checkedEntities = without(this.checkedEntities, entity);
  //   }
  // }
  //
  // isChecked(entity: T): boolean {
  //   return this.checkedEntities.indexOf(entity) > -1;
  // }

  async firstPage(filterData: F) {
    this.filterData = filterData;
    this.entities = [];
    this.currentPage = 0;
    await this.triggerFetch();
    this.onDidChange();
  }

  async nextPage(event: any) {
    if (!this.pagination.isLast) {
      this.currentPage++;
      await this.triggerFetch();
      await event.target.complete();
      this.onDidChange();
    }
  }

  onDidChange() {}

  async refetch() {
    this.currentPage = 0;
    this.entities = [];
    await this.triggerFetch();
  }

  private async triggerFetch() {
    const [entities, pagination] = await this.fetch(
      this.filterData,
      this.currentPage
    );
    this.entities.push(...entities);
    this.pagination = pagination;
  }

  abstract getBulkActionButtons(): ActionSheetButton[];

  abstract fetch(filterData: F, page: number): Promise<[T[], Pagination]>;
}
