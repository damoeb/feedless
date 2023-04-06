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
  ) {}

  async importOpml(uploadEvent: Event) {
    await this.opmlService.convertOpmlToJson(uploadEvent);
  }

  async exportOpml() {
  }
}
