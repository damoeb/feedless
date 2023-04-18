import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ImportOpmlModalComponent } from './import-opml-modal.component';
import { IonicModule } from '@ionic/angular';
import { ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [ImportOpmlModalComponent],
  exports: [ImportOpmlModalComponent],
  imports: [CommonModule, IonicModule, ReactiveFormsModule],
})
export class ImportOpmlModalModule {}
