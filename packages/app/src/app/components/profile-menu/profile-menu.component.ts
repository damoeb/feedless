import { Component, OnInit } from '@angular/core';
import { ModalController, PopoverController } from '@ionic/angular';
import { ProfileSettingsComponent } from '../profile-settings/profile-settings.component';

@Component({
  selector: 'app-profile-menu',
  templateUrl: './profile-menu.component.html',
  styleUrls: ['./profile-menu.component.scss'],
})
export class ProfileMenuComponent implements OnInit {
  constructor(
    private readonly modalController: ModalController,
    private readonly popoverController: PopoverController
  ) {}

  ngOnInit() {}

  async showProfileSettings() {
    this.popoverController.dismiss();
    const modal = await this.modalController.create({
      component: ProfileSettingsComponent,
    });

    await modal.present();
    await modal.onDidDismiss();
  }
}
