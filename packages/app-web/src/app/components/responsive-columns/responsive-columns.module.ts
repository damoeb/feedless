import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule } from '@ionic/angular';
import { ReactiveFormsModule } from '@angular/forms';
import { ResponsiveColumnsComponent } from './responsive-columns.component';

@NgModule({
  declarations: [ResponsiveColumnsComponent],
  exports: [ResponsiveColumnsComponent],
  imports: [CommonModule, IonicModule, ReactiveFormsModule],
})
export class ResponsiveColumnsModule {}
