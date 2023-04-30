import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { ArticlesPage } from './articles.page';
import { PageHeaderModule } from '../../components/page-header/page-header.module';
import { ArticlesPageRoutingModule } from './articles-routing.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    PageHeaderModule,
    ReactiveFormsModule,
    ArticlesPageRoutingModule,
  ],
  declarations: [ArticlesPage],
})
export class ArticlesPageModule {}
