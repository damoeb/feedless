import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { NotebookDetailsPage } from './notebook-details.page';
import { NotebookDetailsRoutingModule } from './notebook-details-routing.module';
import { CodeEditorModule } from '../../../elements/code-editor/code-editor.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { DarkModeButtonModule } from '../../../components/dark-mode-button/dark-mode-button.module';
import { LoginButtonModule } from '../../../components/login-button/login-button.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    NotebookDetailsRoutingModule,
    CodeEditorModule,
    ReactiveFormsModule,
    DarkModeButtonModule,
    FormsModule,
    LoginButtonModule
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [NotebookDetailsPage],
})
export class NotebookDetailsPageModule {}
