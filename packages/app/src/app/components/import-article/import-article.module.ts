import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ImportArticleComponent } from './import-article.component';
import { IonicModule } from '@ionic/angular';
import { FormsModule } from '@angular/forms';

@NgModule({
  declarations: [ImportArticleComponent],
  exports: [ImportArticleComponent],
  imports: [CommonModule, IonicModule, FormsModule],
})
export class ImportArticleModule {}
