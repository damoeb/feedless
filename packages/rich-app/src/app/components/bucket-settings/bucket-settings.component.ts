import { Component, Input, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { Bucket, Subscription } from '../../../generated/graphql';
import { AddSubscriptionComponent } from '../add-subscription/add-subscription.component';

export enum Accordion {}

@Component({
  selector: 'app-bucket-settings',
  templateUrl: './bucket-settings.component.html',
  styleUrls: ['./bucket-settings.component.scss'],
})
export class BucketSettingsComponent implements OnInit {
  @Input()
  bucket: Bucket;
  accordion = {
    filters: 0,
    postProcessors: 1,
    subscription: 2,
  };
  currentAccordion: number;

  constructor(private readonly modalController: ModalController) {}

  ngOnInit() {
    console.log(this.bucket);
  }

  async dismissModal() {
    await this.modalController.dismiss();
  }

  addSubscription() {
    return this.openSubscriptionModal();
  }

  async openSubscriptionModal(subscription?: Subscription) {
    const modal = await this.modalController.create({
      component: AddSubscriptionComponent,
      componentProps: {
        subscription,
      },
    });

    await modal.present();
    modal.onDidDismiss().then(console.log);
  }

  editSubscription(subscription: Subscription) {
    return this.openSubscriptionModal(subscription);
  }

  toggle(accordion: number) {
    this.currentAccordion = accordion;
  }

  isActive(accordeon: number) {
    return this.currentAccordion === accordeon;
  }
}
