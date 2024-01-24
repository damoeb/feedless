import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReaderMenuComponent } from './reader-menu.component';
import { IonicModule } from '@ionic/angular';

@NgModule({
  declarations: [ReaderMenuComponent],
  exports: [ReaderMenuComponent],
  imports: [CommonModule, IonicModule]
})
export class ReaderMenuModule {
}
