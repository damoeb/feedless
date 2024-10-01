import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { DocumentsPageRoutingModule } from './documents-routing.module';

import { DocumentsPage } from './documents.page';
import { FeedlessHeaderModule } from '../../components/feedless-header/feedless-header.module';
import { TermsPage } from './terms.page';
import { TelegramPage } from './telegram.page';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    DocumentsPageRoutingModule,
    FeedlessHeaderModule,
  ],
  declarations: [DocumentsPage, TermsPage, TelegramPage],
})
export class DocumentsPageModule {}
