import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { AboutUntoldNotesRoutingModule } from './about-untold-notes-routing.module';
import { AboutUntoldNotesPage } from './about-untold-notes.page';
import { NotebookBuilderModule } from '../../../components/notebook-builder/notebook-builder.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    AboutUntoldNotesRoutingModule,
    NotebookBuilderModule,
  ],
  declarations: [AboutUntoldNotesPage],
})
export class AboutUntoldNotesModule {}
