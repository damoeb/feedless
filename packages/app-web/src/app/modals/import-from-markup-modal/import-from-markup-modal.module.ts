import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ImportFromMarkupModalComponent } from './import-from-markup-modal.component';
import { IonicModule } from '@ionic/angular';

@NgModule({
  declarations: [ImportFromMarkupModalComponent],
  exports: [ImportFromMarkupModalComponent],
  imports: [CommonModule, IonicModule],
})
export class ImportFromMarkupModalModule {}
