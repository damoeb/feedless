import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { ResponsiveColumnsComponent } from './responsive-columns.component';
import { IonGrid, IonRow, IonCol, IonIcon } from '@ionic/angular/standalone';

@NgModule({
  declarations: [ResponsiveColumnsComponent],
  exports: [ResponsiveColumnsComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    IonGrid,
    IonRow,
    IonCol,
    IonIcon,
  ],
})
export class ResponsiveColumnsModule {}
