import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DocumentsPageRoutingModule } from './documents-routing.module';

import { DocumentsPage } from './documents.page';

import { TermsPage } from './terms.page';
import { TelegramPage } from './telegram.page';
import { IonContent, IonRouterOutlet } from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    DocumentsPageRoutingModule,
    IonContent,
    IonRouterOutlet,
    IonContent,
    IonContent,
    DocumentsPage,
    TermsPage,
    TelegramPage,
],
})
export class DocumentsPageModule {}
