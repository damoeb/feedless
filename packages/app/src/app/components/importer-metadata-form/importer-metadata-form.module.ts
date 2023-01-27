import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ImporterMetadataFormComponent } from './importer-metadata-form.component';
import { IonicModule } from '@ionic/angular';
import { ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [ImporterMetadataFormComponent],
  exports: [ImporterMetadataFormComponent],
  imports: [CommonModule, IonicModule, ReactiveFormsModule],
})
export class ImporterMetadataFormModule {}
