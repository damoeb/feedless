import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {ModalController} from '@ionic/angular';

@Component({
  selector: 'app-bucket-page',
  templateUrl: './bucket.page.html',
  styleUrls: ['./bucket.page.scss'],
})
export class BucketPage  implements OnInit {
  public bucket: string;

  constructor(private activatedRoute: ActivatedRoute,
              private modalController: ModalController) {
  }

  ngOnInit() {
    this.bucket = this.activatedRoute.snapshot.paramMap.get('id');
  }

  async showSettings() {
    // const modal = await this.modalController.create({
    //   component: BucketSettingsPage,
    //   // componentProps: {
    //   //   feedUrl: 'http://gulaschi'
    //   // },
    // });
    //
    // await modal.present();
  }
}
