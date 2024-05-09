import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { AboutUntoldNotesRoutingModule } from './about-untold-notes-routing.module';
import { ProductHeadlineModule } from '../../../components/product-headline/product-headline.module';
import { AboutUntoldNotesPage } from './about-untold-notes.page';
import { SearchbarModule } from '../../../elements/searchbar/searchbar.module';
import { ProductHeaderModule } from '../../../components/product-header/product-header.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    AboutUntoldNotesRoutingModule,
    ProductHeadlineModule,
    SearchbarModule,
    ProductHeaderModule,
  ],
  declarations: [AboutUntoldNotesPage],
})
export class AboutUntoldNotesModule {}
