import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { AboutUntoldNotesRoutingModule } from './about-untold-notes-routing.module';
import { AboutUntoldNotesPage } from './about-untold-notes.page';
import { NotebooksModule } from '../../../components/notebooks/notebooks.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    AboutUntoldNotesRoutingModule,
    NotebooksModule,
  ],
  declarations: [AboutUntoldNotesPage],
})
export class AboutUntoldNotesModule {}
