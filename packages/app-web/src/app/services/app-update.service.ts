import { Injectable } from '@angular/core';
import { SwUpdate } from '@angular/service-worker';
import { AlertController } from '@ionic/angular';

@Injectable({
  providedIn: 'root',
})
export class AppUpdateService {
  constructor(
    private readonly updates: SwUpdate,
    private readonly alertCtrl: AlertController
  ) {
    this.updates.versionUpdates.subscribe((event) => {
      this.showAppUpdateAlert();
    });
  }

  async showAppUpdateAlert() {
    const alert = await this.alertCtrl.create({
      header: 'App Update available',
      backdropDismiss: false,
      message: `Choose 'Update Now' to update`,
      buttons: [
        {
          text: 'Update Now',
          role: 'confirm',
          handler: () => {
            this.doAppUpdate();
          },
        },
      ],
    });

    await alert.present();
  }

  doAppUpdate() {
    this.updates
      .activateUpdate()
      .then(() => document.location.reload())
      .catch(console.error);
  }
}
