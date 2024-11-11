import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotebooksPageRoutingModule } from './notebooks-routing.module';
import { NotebooksPage } from './notebooks.page';
import { NotebooksModule } from '../../components/notebooks/notebooks.module';
import { FeedlessHeaderModule } from '../../components/feedless-header/feedless-header.module';
import { IonContent } from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    NotebooksPageRoutingModule,
    NotebooksModule,
    FeedlessHeaderModule,
    IonContent,
  ],
  declarations: [NotebooksPage],
})
export class NotebooksBuilderPageModule {}
