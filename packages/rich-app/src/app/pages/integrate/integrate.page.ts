import { Component, Input, OnInit } from '@angular/core';
import { GqlArticle } from '../../../generated/graphql';
import { ModalController } from '@ionic/angular';

@Component({
  selector: 'app-integrate',
  templateUrl: './integrate.page.html',
  styleUrls: ['./integrate.page.scss'],
})
export class IntegratePage implements OnInit {
  @Input()
  article: GqlArticle;

  matches: GqlArticle[] = [];
  query = '';

  constructor(private readonly modalController: ModalController) {}

  ngOnInit() {}

  search() {
    console.log('search', this.query);
  }

  dismissModal() {
    return this.modalController.dismiss();
  }
}
