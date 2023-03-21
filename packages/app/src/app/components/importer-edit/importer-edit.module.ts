import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { ImporterEditPage } from './importer-edit.page';
import { BubbleModule } from '../bubble/bubble.module';
import { RouterLink } from '@angular/router';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    ReactiveFormsModule,
    BubbleModule,
    RouterLink,
  ],
  declarations: [ImporterEditPage],
  exports: [ImporterEditPage],
})
export class ImporterEditPageModule {}
