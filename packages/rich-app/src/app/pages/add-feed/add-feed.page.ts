import {Component, OnInit} from '@angular/core';
import {ModalController} from '@ionic/angular';
import {FeedComponent} from "../../components/feed/feed.component";

@Component({
  selector: 'app-add-feed',
  templateUrl: './add-feed.page.html',
  styleUrls: ['./add-feed.page.scss'],
})
export class AddFeedPage implements OnInit {

  constructor(private modalController: ModalController) { }

  ngOnInit() {
  }

  async showFeedModal() {
    const modal = await this.modalController.create({
      component: FeedComponent,
      // componentProps: {
      //   feedUrl: 'http://gulaschi'
      // },
    });

    await modal.present();
  }
}
