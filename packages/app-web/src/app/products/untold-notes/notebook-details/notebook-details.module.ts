import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { NotebookDetailsPage } from './notebook-details.page';
import { NotebookDetailsRoutingModule } from './notebook-details-routing.module';
import { CodeEditorModule } from '../../../elements/code-editor/code-editor.module';
import { ReactiveFormsModule } from '@angular/forms';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    NotebookDetailsRoutingModule,
    CodeEditorModule,
    ReactiveFormsModule,
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [NotebookDetailsPage],
})
export class NotebookDetailsPageModule {}
