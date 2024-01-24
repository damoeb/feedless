import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { VisualDiffMenuComponent } from './visual-diff-menu.component';
import { IonicModule } from '@ionic/angular';
import { RouterLink } from '@angular/router';

@NgModule({
  declarations: [VisualDiffMenuComponent],
  exports: [VisualDiffMenuComponent],
  imports: [CommonModule, IonicModule, RouterLink]
})
export class VisualDiffMenuModule {
}
