import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MenuComponent } from './menu.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {
  IonPopover,
  IonContent,
  IonHeader,
  IonSearchbar,
  IonList,
  IonItem,
  IonLabel,
  IonButton,
} from '@ionic/angular/standalone';

@NgModule({
  declarations: [MenuComponent],
  exports: [MenuComponent],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    IonPopover,
    IonContent,
    IonHeader,
    IonSearchbar,
    IonList,
    IonItem,
    IonLabel,
    IonButton,
  ],
})
export class MenuModule {}
