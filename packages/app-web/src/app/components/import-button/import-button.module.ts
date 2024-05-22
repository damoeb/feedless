import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ImportButtonComponent } from './import-button.component';
import { IonicModule } from '@ionic/angular';
import { BubbleModule } from '../bubble/bubble.module';
import { RouterLink } from '@angular/router';

@NgModule({
  declarations: [ImportButtonComponent],
  exports: [ImportButtonComponent],
  imports: [CommonModule, IonicModule, BubbleModule, RouterLink]
})
export class ImportButtonModule {}
