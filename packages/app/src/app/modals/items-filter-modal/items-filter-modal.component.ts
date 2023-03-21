import { Component, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';

export interface ItemsFilterModalComponentProps {
  filterExpression: string;
}

@Component({
  selector: 'app-items-filter-modal',
  templateUrl: './items-filter-modal.component.html',
  styleUrls: ['./items-filter-modal.component.scss'],
})
export class ItemsFilterModalComponent
  implements OnInit, ItemsFilterModalComponentProps
{
  filterExpression: string;

  constructor(private readonly modalCtrl: ModalController) {}

  ngOnInit() {}

  cancel() {
    return this.modalCtrl.dismiss();
  }

  useFilter() {
    return this.modalCtrl.dismiss(this.filterExpression, 'persist');
  }

  clear() {
    return this.modalCtrl.dismiss('', 'clear');
  }
}
