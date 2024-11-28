import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotebooksPageRoutingModule } from './notebooks-routing.module';
import { NotebooksPage } from './notebooks.page';


import { IonContent } from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    NotebooksPageRoutingModule,
    IonContent,
    NotebooksPage,
],
})
export class NotebooksBuilderPageModule {}
