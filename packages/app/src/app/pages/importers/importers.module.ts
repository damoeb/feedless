import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { ImportersPageRoutingModule } from './importers-routing.module';

import { ImportersPage } from './importers.page';
import { BubbleModule } from '../../components/bubble/bubble.module';
import { ArticleRefModule } from '../../components/article-ref/article-ref.module';
import { ImporterEditPageModule } from '../../components/importer-create/importer-create.module';
import { FilterToolbarModule } from '../../components/filter-toolbar/filter-toolbar.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    ImportersPageRoutingModule,
    ReactiveFormsModule,
    BubbleModule,
    ArticleRefModule,
    ImporterEditPageModule,
    FilterToolbarModule,
  ],
  declarations: [ImportersPage],
})
export class ImportersModule {}
