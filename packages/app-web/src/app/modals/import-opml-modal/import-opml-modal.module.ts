import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule } from '@ionic/angular';
import { RouterLink } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ImportOpmlModalComponent } from './import-opml-modal.component';

@NgModule({
  declarations: [ImportOpmlModalComponent],
  exports: [ImportOpmlModalComponent],
  imports: [
    CommonModule,
    IonicModule,
    RouterLink,
    FormsModule,
    ReactiveFormsModule,
  ],
})
export class ImportOpmlModalModule {}
