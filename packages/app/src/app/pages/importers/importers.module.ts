import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { BucketEditPageRoutingModule } from './importers-routing.module';

import { ImportersPage } from './importers.page';
import { BubbleModule } from '../../components/bubble/bubble.module';
import { ArticleModule } from '../../components/article/article.module';
import { ImporterEditPageModule } from '../../components/importer-create/importer-create.module';
import { FilterToolbarModule } from '../../components/filter-toolbar/filter-toolbar.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    BucketEditPageRoutingModule,
    ReactiveFormsModule,
    BubbleModule,
    ArticleModule,
    ImporterEditPageModule,
    FilterToolbarModule
  ],
  declarations: [ImportersPage],
})
export class ImportersModule {}
