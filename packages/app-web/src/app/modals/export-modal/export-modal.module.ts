import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ExportModalComponent } from './export-modal.component';
import { IonicModule } from '@ionic/angular';

@NgModule({
  declarations: [ExportModalComponent],
  exports: [ExportModalComponent],
  imports: [CommonModule, IonicModule]
})
export class ExportModalModule {
}
