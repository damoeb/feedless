import { Component, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';

@Component({
  selector: 'app-output-throttle',
  templateUrl: './output-throttle.component.html',
  styleUrls: ['./output-throttle.component.scss'],
})
export class OutputThrottleComponent implements OnInit {
  constructor(private readonly modalController: ModalController) {}

  ngOnInit() {}

  dismissModal() {
    return this.modalController.dismiss();
  }
}
