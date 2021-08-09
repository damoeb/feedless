import { Component, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';

@Component({
  selector: 'app-filters',
  templateUrl: './filters.component.html',
  styleUrls: ['./filters.component.scss'],
})
export class FiltersComponent implements OnInit {
  constructor(private readonly modalController: ModalController) {}

  ngOnInit() {}

  dismissModal() {
    return this.modalController.dismiss();
  }
}
