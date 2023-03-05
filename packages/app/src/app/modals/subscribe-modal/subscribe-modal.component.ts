import { Component, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';

@Component({
  selector: 'app-subscribe-modal',
  templateUrl: './subscribe-modal.component.html',
  styleUrls: ['./subscribe-modal.component.scss'],
})
export class SubscribeModalComponent implements OnInit {
  constructor(private readonly modalCtrl: ModalController) {}

  ngOnInit() {}

  dismiss() {
    return this.modalCtrl.dismiss();
  }
}
