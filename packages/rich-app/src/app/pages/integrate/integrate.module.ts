import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { IntegratePageRoutingModule } from './integrate-routing.module';

import { IntegratePage } from './integrate.page';
import { ArticleModule } from '../../components/article/article.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    IntegratePageRoutingModule,
    ArticleModule,
  ],
  declarations: [IntegratePage],
})
export class IntegratePageModule {}
