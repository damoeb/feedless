import { Component, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { ModalDismissal } from '../../app.module';

export interface ImportArticleComponentProps {
  articleId: string;
}

@Component({
  selector: 'app-import-article',
  templateUrl: './import-article.component.html',
  styleUrls: ['./import-article.component.scss'],
})
export class ImportArticleComponent
  implements OnInit, ImportArticleComponentProps
{
  articleId: string;

  constructor(private readonly modalCtrl: ModalController) {}

  ngOnInit() {}

  searchBucket() {}

  closeModal() {
    const response: ModalDismissal = {
      cancel: true,
    };
    return this.modalCtrl.dismiss(response);
  }
}
