import { Component, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';

@Component({
  selector: 'app-import-article',
  templateUrl: './import-article.component.html',
  styleUrls: ['./import-article.component.scss'],
})
export class ImportArticleComponent implements OnInit {
  query: string;

  constructor(private readonly modalCtrl: ModalController) { }

  ngOnInit() {}

  searchBucket() {

  }

  closeModal() {
    return this.modalCtrl.dismiss()
  }
}
