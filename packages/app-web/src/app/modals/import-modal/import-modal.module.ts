import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ImportModalComponent } from './import-modal.component';
import { IonicModule } from '@ionic/angular';
import { ImportOpmlModalModule } from '../import-opml-modal/import-opml-modal.module';
import { ImportFromMarkupModalModule } from '../import-from-markup-modal/import-from-markup-modal.module';

@NgModule({
  declarations: [ImportModalComponent],
  exports: [ImportModalComponent],
  imports: [
    CommonModule,
    IonicModule,
    ImportOpmlModalModule,
    ImportFromMarkupModalModule,
  ],
})
export class ImportModalModule {}
