import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { ImporterEditPageRoutingModule } from './importer-edit-routing.module';

import { ImporterEditPage } from './importer-edit.page';
import { BubbleModule } from '../bubble/bubble.module';
import { ArticleModule } from '../article/article.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    ImporterEditPageRoutingModule,
    ReactiveFormsModule,
    BubbleModule,
    ArticleModule
  ],
  declarations: [ImporterEditPage]
})
export class ImporterEditPageModule {}
