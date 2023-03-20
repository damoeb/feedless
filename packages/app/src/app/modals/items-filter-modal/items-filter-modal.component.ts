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
export class ItemsFilterModalComponent implements OnInit, ItemsFilterModalComponentProps {

  filterExpression: string;

  constructor(private readonly modalCtrl: ModalController) { }

  ngOnInit() {}

  dismiss() {
    return this.modalCtrl.dismiss();
  }
}
