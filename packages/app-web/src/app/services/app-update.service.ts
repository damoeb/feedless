import { Injectable } from '@angular/core';
import { SwUpdate, VersionReadyEvent } from '@angular/service-worker';
import { AlertController } from '@ionic/angular';
import { filter, interval, throttle } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AppUpdateService {
  constructor(
    private readonly swUpdate: SwUpdate,
    private readonly alertCtrl: AlertController
  ) {
    this.swUpdate.versionUpdates
      .pipe(
        filter((evt): evt is VersionReadyEvent => evt.type === 'VERSION_READY')
      )
      .pipe(throttle(() => interval(3000)))
      .subscribe(async (event) => {
        console.log(event);
        await this.showAppUpdateAlert();
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
          }
        }
      ]
    });

    await alert.present();
  }

  doAppUpdate() {
    this.swUpdate
      .activateUpdate()
      .then(() => document.location.reload())
      .catch(console.error);
  }
}
