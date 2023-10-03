import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { VisualDiffMenuComponent } from './visual-diff-menu.component';
import { IonicModule } from '@ionic/angular';

@NgModule({
  declarations: [VisualDiffMenuComponent],
  exports: [VisualDiffMenuComponent],
  imports: [CommonModule, IonicModule],
})
export class VisualDiffMenuModule {}
