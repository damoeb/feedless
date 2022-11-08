import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { BucketEditPageRoutingModule } from './bucket-edit-routing.module';

import { BucketEditPage } from './bucket-edit.page';
import { BubbleModule } from '../bubble/bubble.module';
import { ArticleModule } from '../article/article.module';
import { ImporterEditPageModule } from '../importer-create/importer-create.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    BucketEditPageRoutingModule,
    ReactiveFormsModule,
    BubbleModule,
    ArticleModule,
    ImporterEditPageModule
  ],
  declarations: [BucketEditPage]
})
export class BucketEditPageModule {}
