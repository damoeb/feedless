import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ImportButtonComponent } from './import-button.component';
import { BubbleModule } from '../bubble/bubble.module';
import { RouterLink } from '@angular/router';
import {
  IonButton,
  IonLabel,
  IonPopover,
  IonContent,
  IonList,
  IonItem,
  ModalController,
} from '@ionic/angular/standalone';
import { RemoveIfProdModule } from '../../directives/remove-if-prod/remove-if-prod.module';

@NgModule({
  declarations: [ImportButtonComponent],
  exports: [ImportButtonComponent],
  providers: [ModalController],
  imports: [
    CommonModule,
    BubbleModule,
    RouterLink,
    IonButton,
    IonLabel,
    IonPopover,
    IonContent,
    IonList,
    IonItem,
    RemoveIfProdModule,
  ],
})
export class ImportButtonModule {}
