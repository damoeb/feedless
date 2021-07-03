import { Component, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { SearchPage } from '../search/search.page';
import { validURL } from '../../utils';
import { Router } from '@angular/router';

@Component({
  selector: 'app-toolbar',
  templateUrl: './toolbar.component.html',
  styleUrls: ['./toolbar.component.scss'],
})
export class ToolbarComponent implements OnInit {
  query: string;

  constructor(
    private readonly modalController: ModalController,
    private readonly router: Router
  ) {}

  ngOnInit() {}

  async searchOrReader() {
    if (validURL(this.query)) {
      await this.router.navigateByUrl(
        `reader?url=${encodeURIComponent(this.query)}`
      );
    } else {
      const modal = await this.modalController.create({
        component: SearchPage,
        // componentProps: {
        //   subscription,
        // },
      });

      await modal.present();
    }
  }
}
