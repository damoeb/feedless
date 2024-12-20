import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReaderMenuComponent } from './reader-menu.component';
import {
  IonItem,
  IonLabel,
  IonList,
  IonListHeader,
} from '@ionic/angular/standalone';

@NgModule({
  declarations: [ReaderMenuComponent],
  exports: [ReaderMenuComponent],
  imports: [CommonModule, IonList, IonListHeader, IonItem, IonLabel],
})
export class ReaderMenuModule {}
