import { Component, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';

@Component({
  selector: 'app-profile-settings',
  templateUrl: './profile-settings.component.html',
  styleUrls: ['./profile-settings.component.scss'],
})
export class ProfileSettingsComponent implements OnInit {
  useReadability: any;
  constructor(private readonly modalController: ModalController) {}

  ngOnInit() {}

  dismissModal() {
    return this.modalController.dismiss();
  }

  save() {
    // this.bucketService.updateFilterExpression(this.bucket.id, this.expression);
  }
}
