import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AboutUntoldNotesRoutingModule } from './about-untold-notes-routing.module';
import { AboutUntoldNotesPage } from './about-untold-notes.page';

import { IonContent } from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    AboutUntoldNotesRoutingModule,
    IonContent,
    AboutUntoldNotesPage,
],
})
export class AboutUntoldNotesModule {}
