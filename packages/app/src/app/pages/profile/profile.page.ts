import { Component } from '@angular/core';
import { OpmlService } from '../../services/opml.service';
import { ToastController } from '@ionic/angular';
import { ProfileService } from '../../services/profile.service';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.page.html',
  styleUrls: ['./profile.page.scss'],
})
export class ProfilePage {
  constructor(
    private readonly opmlService: OpmlService,
    private readonly profileService: ProfileService,
    private readonly toastCtrl: ToastController
  ) {}

  async importOpml(uploadEvent: Event) {
    const reader = new FileReader();

    const files = Array.from((uploadEvent.target as any).files).map(
      (file: File) =>
        new Promise<void>((resolve) => {
          reader.onloadend = async (event) => {
            const data: ArrayBuffer | string = (event.target as any).result;
            await this.opmlService.importOpml({
              data: String(data),
            });
            resolve();
          };
          reader.readAsText(file);
        })
    );

    await Promise.all(files);
    const toast = await this.toastCtrl.create({
      message: 'Imported',
      duration: 4000,
      color: 'success',
    });
    await toast.present();
  }

  async exportOpml() {
    const opml = await this.opmlService.exportOpml();
    console.log(opml);
  }
}
