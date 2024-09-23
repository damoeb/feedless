import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';

import { NotebooksPageRoutingModule } from './notebooks-routing.module';
import { NotebooksPage } from './notebooks.page';
import { NotebooksModule } from '../../components/notebooks/notebooks.module';
import { FeedlessHeaderModule } from '../../components/feedless-header/feedless-header.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    NotebooksPageRoutingModule,
    NotebooksModule,
    FeedlessHeaderModule,
  ],
  declarations: [NotebooksPage],
})
export class NotebooksBuilderPageModule {}
